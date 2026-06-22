package cn.iesst.demo.service;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class PrivateDownloadResponseFactory {
    public ResponseEntity<?> create(ManuscriptStorageService.DownloadTarget target) {
        if (target.redirectUrl() != null) {
            return ResponseEntity.status(302)
                    .location(URI.create(target.redirectUrl()))
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(target.contentType());
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(target.fileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(target.resource());
    }
}
