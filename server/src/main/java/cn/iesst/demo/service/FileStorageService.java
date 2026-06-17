package cn.iesst.demo.service;

import cn.iesst.demo.model.UploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) throws IOException {
        root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root.resolve("images"));
        Files.createDirectories(root.resolve("documents"));
    }

    public UploadResult saveImage(MultipartFile file) {
        return save(file, "images", IMAGE_EXTENSIONS, 10 * 1024 * 1024L);
    }

    public UploadResult saveDocument(MultipartFile file) {
        return save(file, "documents", DOCUMENT_EXTENSIONS, 20 * 1024 * 1024L);
    }

    private UploadResult save(MultipartFile file, String folder, Set<String> allowed, long maxSize) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择需要上传的文件");
        if (file.getSize() > maxSize) throw new IllegalArgumentException("文件大小超出限制");
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = extension(original);
        if (!allowed.contains(extension)) throw new IllegalArgumentException("不支持该文件格式");
        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = root.resolve(folder).resolve(storedName).normalize();
        if (!destination.startsWith(root)) throw new IllegalArgumentException("文件路径非法");
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("文件保存失败", exception);
        }
        return new UploadResult(original, "/uploads/" + folder + "/" + storedName, file.getSize());
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
