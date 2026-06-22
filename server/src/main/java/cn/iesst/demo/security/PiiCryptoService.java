package cn.iesst.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PiiCryptoService {
    public static final String DEV_ENCRYPTION_KEY = "9Bethbu5tzCygaJT47X73WcQ2oaGXPXSzLaW0ObFGHo=";
    public static final String DEV_LOOKUP_KEY = "ZKTvfWmq/HWuxjY6NdEzGJLg4fwiOjhl8Q38RkxKaRI=";

    private final MobileCrypto crypto;

    public PiiCryptoService(
            @Value("${app.pii.encryption-key}") String encryptionKey,
            @Value("${app.pii.lookup-key}") String lookupKey) {
        this.crypto = new MobileCrypto(encryptionKey, lookupKey);
    }

    public String encryptMobile(String mobile) {
        return crypto.encrypt(mobile);
    }

    public String decryptMobile(String ciphertext) {
        return crypto.decrypt(ciphertext);
    }

    public String mobileLookupHash(String mobile) {
        return crypto.lookupHash(mobile);
    }

    public String mobileLastFour(String mobile) {
        return crypto.lastFour(mobile);
    }

    public String normalizeMobile(String mobile) {
        return MobileCrypto.normalize(mobile);
    }
}
