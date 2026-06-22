package cn.iesst.demo.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MobileCryptoTest {
    private static final String ENCRYPTION_KEY = "9Bethbu5tzCygaJT47X73WcQ2oaGXPXSzLaW0ObFGHo=";
    private static final String LOOKUP_KEY = "ZKTvfWmq/HWuxjY6NdEzGJLg4fwiOjhl8Q38RkxKaRI=";

    @Test
    void encryptsWithRandomIvAndDecryptsToNormalizedMobile() {
        MobileCrypto crypto = new MobileCrypto(ENCRYPTION_KEY, LOOKUP_KEY);

        String first = crypto.encrypt("188 0000-0088");
        String second = crypto.encrypt("18800000088");

        assertNotEquals(first, second);
        assertEquals("18800000088", crypto.decrypt(first));
        assertEquals("18800000088", crypto.decrypt(second));
    }

    @Test
    void producesStableLookupHashWithoutExposingMobile() {
        MobileCrypto crypto = new MobileCrypto(ENCRYPTION_KEY, LOOKUP_KEY);

        String hash = crypto.lookupHash("18800000088");

        assertEquals(hash, crypto.lookupHash("188 0000 0088"));
        assertEquals(64, hash.length());
        assertNotEquals("18800000088", hash);
        assertEquals("0088", crypto.lastFour("18800000088"));
    }

    @Test
    void rejectsCiphertextWhenEncryptionKeyDoesNotMatch() {
        MobileCrypto original = new MobileCrypto(ENCRYPTION_KEY, LOOKUP_KEY);
        MobileCrypto different = new MobileCrypto(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                LOOKUP_KEY);

        String ciphertext = original.encrypt("18800000088");

        assertThrows(IllegalStateException.class, () -> different.decrypt(ciphertext));
    }
}
