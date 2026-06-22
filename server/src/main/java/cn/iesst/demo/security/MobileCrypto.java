package cn.iesst.demo.security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class MobileCrypto {
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec lookupKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public MobileCrypto(String encryptionKeyBase64, String lookupKeyBase64) {
        byte[] encryptionBytes = decodeKey(encryptionKeyBase64, "PII_ENCRYPTION_KEY");
        byte[] lookupBytes = decodeKey(lookupKeyBase64, "PII_LOOKUP_KEY");
        if (encryptionBytes.length != 32 || lookupBytes.length != 32) {
            throw new IllegalStateException("PII encryption and lookup keys must each contain 32 bytes");
        }
        this.encryptionKey = new SecretKeySpec(encryptionBytes, "AES");
        this.lookupKey = new SecretKeySpec(lookupBytes, "HmacSHA256");
    }

    public String encrypt(String mobile) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(normalize(mobile).getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return "v1." + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to encrypt student mobile", exception);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null || !ciphertext.startsWith("v1.")) {
            throw new IllegalStateException("Unsupported student mobile ciphertext");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext.substring(3));
            if (payload.length <= GCM_IV_BYTES) {
                throw new IllegalStateException("Invalid student mobile ciphertext");
            }
            byte[] iv = new byte[GCM_IV_BYTES];
            byte[] encrypted = new byte[payload.length - GCM_IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to decrypt student mobile", exception);
        }
    }

    public String lookupHash(String mobile) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(lookupKey);
            return HexFormat.of().formatHex(mac.doFinal(normalize(mobile).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to hash student mobile", exception);
        }
    }

    public String lastFour(String mobile) {
        String normalized = normalize(mobile);
        return normalized.substring(Math.max(0, normalized.length() - 4));
    }

    public static String normalize(String mobile) {
        if (mobile == null) return "";
        return mobile.replaceAll("[\\s-]", "").trim();
    }

    private byte[] decodeKey(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(name + " must be Base64 encoded", exception);
        }
    }
}
