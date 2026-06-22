package db.migration;

import cn.iesst.demo.security.MobileCrypto;
import cn.iesst.demo.security.PiiCryptoService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class V7__encrypt_student_mobile extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        MobileCrypto crypto = new MobileCrypto(
                key("PII_ENCRYPTION_KEY", PiiCryptoService.DEV_ENCRYPTION_KEY),
                key("PII_LOOKUP_KEY", PiiCryptoService.DEV_LOOKUP_KEY));

        try (Statement statement = context.getConnection().createStatement()) {
            statement.execute("ALTER TABLE student_users " +
                    "ADD COLUMN mobile_ciphertext VARCHAR(255) NULL, " +
                    "ADD COLUMN mobile_lookup_hash CHAR(64) NULL, " +
                    "ADD COLUMN mobile_last4 VARCHAR(4) NULL, " +
                    "ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0, " +
                    "ADD COLUMN locked_until DATETIME NULL, " +
                    "ADD COLUMN last_login_at DATETIME NULL");
            statement.execute("ALTER TABLE consultation_records ADD COLUMN mobile_ciphertext VARCHAR(255) NULL");
            statement.execute("ALTER TABLE invoice_requests ADD COLUMN receiver_phone_ciphertext VARCHAR(255) NULL");
        }

        try (PreparedStatement select = context.getConnection().prepareStatement("SELECT id,mobile FROM student_users");
             ResultSet rows = select.executeQuery();
             PreparedStatement update = context.getConnection().prepareStatement(
                     "UPDATE student_users SET mobile_ciphertext=?,mobile_lookup_hash=?,mobile_last4=? WHERE id=?")) {
            while (rows.next()) {
                String mobile = MobileCrypto.normalize(rows.getString("mobile"));
                update.setString(1, crypto.encrypt(mobile));
                update.setString(2, crypto.lookupHash(mobile));
                update.setString(3, crypto.lastFour(mobile));
                update.setLong(4, rows.getLong("id"));
                update.addBatch();
            }
            update.executeBatch();
        }

        encryptOptionalColumn(
                context,
                crypto,
                "SELECT id,mobile FROM consultation_records WHERE mobile IS NOT NULL AND mobile<>''",
                "UPDATE consultation_records SET mobile_ciphertext=? WHERE id=?");
        encryptOptionalColumn(
                context,
                crypto,
                "SELECT id,receiver_phone FROM invoice_requests WHERE receiver_phone IS NOT NULL AND receiver_phone<>''",
                "UPDATE invoice_requests SET receiver_phone_ciphertext=? WHERE id=?");

        try (Statement statement = context.getConnection().createStatement()) {
            statement.execute("ALTER TABLE student_users " +
                    "MODIFY mobile_ciphertext VARCHAR(255) NOT NULL, " +
                    "MODIFY mobile_lookup_hash CHAR(64) NOT NULL, " +
                    "MODIFY mobile_last4 VARCHAR(4) NOT NULL, " +
                    "ADD UNIQUE INDEX uk_student_users_mobile_lookup (mobile_lookup_hash), " +
                    "DROP COLUMN mobile");
            statement.execute("ALTER TABLE consultation_records DROP COLUMN mobile");
            statement.execute("ALTER TABLE invoice_requests DROP COLUMN receiver_phone");
        }
    }

    private void encryptOptionalColumn(
            Context context,
            MobileCrypto crypto,
            String selectSql,
            String updateSql) throws Exception {
        try (PreparedStatement select = context.getConnection().prepareStatement(selectSql);
             ResultSet rows = select.executeQuery();
             PreparedStatement update = context.getConnection().prepareStatement(updateSql)) {
            while (rows.next()) {
                update.setString(1, crypto.encrypt(rows.getString(2)));
                update.setLong(2, rows.getLong(1));
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private String key(String name, String developmentDefault) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) return value;
        String profiles = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "");
        if (profiles.contains("prod")) {
            throw new IllegalStateException(name + " is required when the prod profile is active");
        }
        return developmentDefault;
    }
}
