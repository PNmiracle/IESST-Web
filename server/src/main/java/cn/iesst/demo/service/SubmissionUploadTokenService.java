package cn.iesst.demo.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class SubmissionUploadTokenService {
    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    public SubmissionUploadTokenService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String issue(long submissionId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update(
                "UPDATE submissions SET upload_token_hash=?,upload_token_expires_at=?,upload_token_used_at=NULL WHERE id=?",
                hash(token),
                LocalDateTime.now().plusMinutes(10),
                submissionId);
        return token;
    }

    public void claim(long submissionId, String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("稿件上传凭证缺失，请重新提交评估信息");
        int updated = jdbc.update(
                "UPDATE submissions SET upload_token_used_at=CURRENT_TIMESTAMP " +
                        "WHERE id=? AND upload_token_hash=? AND upload_token_expires_at>CURRENT_TIMESTAMP AND upload_token_used_at IS NULL",
                submissionId,
                hash(token));
        if (updated == 0) throw new IllegalArgumentException("稿件上传凭证无效或已过期，请重新提交评估信息");
    }

    public void release(long submissionId) {
        jdbc.update("UPDATE submissions SET upload_token_used_at=NULL WHERE id=?", submissionId);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
