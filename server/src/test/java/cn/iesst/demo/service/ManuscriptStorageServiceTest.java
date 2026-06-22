package cn.iesst.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManuscriptStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void storesPdfOutsidePublicUploadDirectory() throws Exception {
        ManuscriptStorageService service = service();
        var result = service.save(new MockMultipartFile(
                "file", "paper.pdf", "text/plain", "%PDF-1.7\ncontent".getBytes(StandardCharsets.US_ASCII)));

        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.storageKey()).startsWith("manuscripts/");
        assertThat(Files.exists(tempDir.resolve("private").resolve(result.storageKey()))).isTrue();
        assertThat(Files.exists(tempDir.resolve("public").resolve(result.storageKey()))).isFalse();
    }

    @Test
    void rejectsTextRenamedAsPdf() throws Exception {
        ManuscriptStorageService service = service();
        MockMultipartFile file = new MockMultipartFile(
                "file", "paper.pdf", "application/pdf", "not a pdf".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.save(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("扩展名不匹配");
    }

    @Test
    void acceptsOnlyDocxArchivesWithWordDocument() throws Exception {
        ManuscriptStorageService service = service();
        var valid = service.save(new MockMultipartFile(
                "file", "paper.docx", "application/zip", docxBytes(true)));
        assertThat(valid.contentType()).contains("wordprocessingml");

        MockMultipartFile fake = new MockMultipartFile(
                "file", "fake.docx", "application/zip", docxBytes(false));
        assertThatThrownBy(() -> service.save(fake)).isInstanceOf(IllegalArgumentException.class);
    }

    private ManuscriptStorageService service() throws Exception {
        MalwareScanService scanner = new MalwareScanService(false, "127.0.0.1", 3310, Duration.ofSeconds(1));
        return new ManuscriptStorageService(
                "local",
                tempDir.resolve("private").toString(),
                tempDir.resolve("public").toString(),
                300,
                "", "", "", "", "manuscripts",
                scanner);
    }

    private byte[] docxBytes(boolean includeDocument) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            if (includeDocument) {
                zip.putNextEntry(new ZipEntry("word/document.xml"));
                zip.write("<document/>".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
