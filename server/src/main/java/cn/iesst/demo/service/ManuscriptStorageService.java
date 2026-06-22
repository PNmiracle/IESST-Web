package cn.iesst.demo.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ManuscriptStorageService {
    private static final long MAX_SIZE = 20 * 1024 * 1024L;
    private static final Set<String> EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final String backend;
    private final Path privateRoot;
    private final Path legacyUploadRoot;
    private final long signedUrlSeconds;
    private final String bucket;
    private final String prefix;
    private final MalwareScanService malwareScanService;
    private final OSS oss;

    public ManuscriptStorageService(
            @Value("${app.manuscript-storage.backend:local}") String backend,
            @Value("${app.private-upload-dir:./private-uploads}") String privateUploadDir,
            @Value("${app.upload-dir:./uploads}") String legacyUploadDir,
            @Value("${app.manuscript-storage.signed-url-seconds:300}") long signedUrlSeconds,
            @Value("${app.manuscript-storage.oss.endpoint:}") String endpoint,
            @Value("${app.manuscript-storage.oss.bucket:}") String bucket,
            @Value("${app.manuscript-storage.oss.access-key-id:}") String accessKeyId,
            @Value("${app.manuscript-storage.oss.access-key-secret:}") String accessKeySecret,
            @Value("${app.manuscript-storage.oss.prefix:manuscripts}") String prefix,
            MalwareScanService malwareScanService) throws IOException {
        this.backend = backend.trim().toLowerCase(Locale.ROOT);
        this.privateRoot = Path.of(privateUploadDir).toAbsolutePath().normalize();
        this.legacyUploadRoot = Path.of(legacyUploadDir).toAbsolutePath().normalize();
        this.signedUrlSeconds = Math.min(Math.max(signedUrlSeconds, 60), 900);
        this.bucket = bucket;
        this.prefix = prefix.replaceAll("^/+|/+$", "");
        this.malwareScanService = malwareScanService;
        if ("oss".equals(this.backend)) {
            require(endpoint, "OSS_ENDPOINT");
            require(bucket, "OSS_BUCKET");
            require(accessKeyId, "OSS_ACCESS_KEY_ID");
            require(accessKeySecret, "OSS_ACCESS_KEY_SECRET");
            this.oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        } else if ("local".equals(this.backend)) {
            Files.createDirectories(this.privateRoot.resolve("manuscripts"));
            this.oss = null;
        } else {
            throw new IllegalStateException("MANUSCRIPT_STORAGE_BACKEND 仅支持 local 或 oss");
        }
    }

    public StoredManuscript save(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的稿件");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("稿件文件不能超过 20MB");
        String originalName = safeName(file.getOriginalFilename());
        String extension = extension(originalName);
        if (!EXTENSIONS.contains(extension)) throw new IllegalArgumentException("仅支持 PDF、DOC、DOCX 文件");
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("稿件文件读取失败", exception);
        }
        String contentType = verifyContent(extension, content);
        malwareScanService.scan(content);
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String key = prefix + "/" + now.getYear() + "/" + String.format("%02d", now.getMonthValue()) + "/" + UUID.randomUUID() + "." + extension;
        if (oss != null) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            metadata.setContentType(contentType);
            metadata.setContentDisposition("attachment; filename*=UTF-8''" + urlEncode(originalName));
            oss.putObject(bucket, key, new ByteArrayInputStream(content), metadata);
        } else {
            Path destination = resolvePrivate(key);
            try {
                Files.createDirectories(destination.getParent());
                Files.copy(new ByteArrayInputStream(content), destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new IllegalStateException("稿件文件保存失败", exception);
            }
        }
        return new StoredManuscript(originalName, key, content.length, contentType);
    }

    public DownloadTarget download(String storageKey, String fileName, String contentType) {
        if (oss != null) {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, normalizeKey(storageKey), HttpMethod.GET);
            request.setExpiration(Date.from(Instant.now().plusSeconds(signedUrlSeconds)));
            URL url = oss.generatePresignedUrl(request);
            return new DownloadTarget(null, url.toString(), fileName, contentType);
        }
        Path path = resolveExistingLocal(storageKey);
        try {
            Resource resource = new InputStreamResource(Files.newInputStream(path));
            return new DownloadTarget(resource, null, fileName, contentType);
        } catch (IOException exception) {
            throw new IllegalArgumentException("稿件文件不存在或已归档");
        }
    }

    private String verifyContent(String extension, byte[] content) {
        if ("pdf".equals(extension) && startsWith(content, new byte[]{'%', 'P', 'D', 'F', '-'})) return "application/pdf";
        if ("doc".equals(extension) && startsWith(content, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1})) {
            return "application/msword";
        }
        if ("docx".equals(extension) && isDocx(content)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        throw new IllegalArgumentException("文件内容与扩展名不匹配");
    }

    private boolean isDocx(byte[] content) {
        boolean contentTypes = false;
        boolean document = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("[Content_Types].xml".equals(entry.getName())) contentTypes = true;
                if ("word/document.xml".equals(entry.getName())) document = true;
                if (contentTypes && document) return true;
            }
        } catch (IOException ignored) {
            return false;
        }
        return false;
    }

    private Path resolveExistingLocal(String storageKey) {
        if (storageKey.startsWith("/uploads/manuscripts/")) {
            Path legacy = legacyUploadRoot.resolve("manuscripts").resolve(Path.of(storageKey).getFileName()).normalize();
            if (legacy.startsWith(legacyUploadRoot) && Files.exists(legacy)) return legacy;
        }
        Path path = resolvePrivate(normalizeKey(storageKey));
        if (!Files.exists(path)) throw new IllegalArgumentException("稿件文件不存在或已归档");
        return path;
    }

    private Path resolvePrivate(String key) {
        Path path = privateRoot.resolve(key).normalize();
        if (!path.startsWith(privateRoot)) throw new IllegalArgumentException("稿件存储路径非法");
        return path;
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("稿件存储信息缺失");
        return key.replaceFirst("^/+", "");
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) if (content[i] != signature[i]) return false;
        return true;
    }

    private String safeName(String name) {
        String cleaned = (name == null ? "manuscript" : name).replaceAll("[\\r\\n\\u0000]", "_");
        String value = Path.of(cleaned).getFileName().toString();
        return value.isBlank() ? "manuscript" : value;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required when OSS storage is enabled");
    }

    @PreDestroy
    void shutdown() {
        if (oss != null) oss.shutdown();
    }

    public record StoredManuscript(String fileName, String storageKey, long size, String contentType) {}
    public record DownloadTarget(Resource resource, String redirectUrl, String fileName, String contentType) {}
}
