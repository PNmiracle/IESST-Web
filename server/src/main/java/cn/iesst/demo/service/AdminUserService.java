package cn.iesst.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService implements UserDetailsService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUsername;
    private final String defaultPassword;
    private final String defaultDisplayName;

    public AdminUserService(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String defaultUsername,
            @Value("${app.admin.password}") String defaultPassword,
            @Value("${app.admin.display-name}") String defaultDisplayName) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
        this.defaultDisplayName = defaultDisplayName;
    }

    @PostConstruct
    void ensureDefaultAdministrator() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM admin_users", Integer.class);
        if (count != null && count == 0) {
            jdbc.update(
                    "INSERT INTO admin_users(username,password_hash,display_name,role_name,enabled) VALUES (?,?,?,?,TRUE)",
                    defaultUsername,
                    passwordEncoder.encode(defaultPassword),
                    defaultDisplayName,
                    "ADMIN");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return jdbc.query(
                        "SELECT username,password_hash,role_name,enabled FROM admin_users WHERE username=?",
                        (rs, rowNum) -> User.withUsername(rs.getString("username"))
                                .password(rs.getString("password_hash"))
                                .roles(rs.getString("role_name"))
                                .disabled(!rs.getBoolean("enabled"))
                                .build(),
                        username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("管理员账号不存在"));
    }

    public String displayName(String username) {
        return jdbc.query(
                        "SELECT display_name FROM admin_users WHERE username=?",
                        (rs, rowNum) -> rs.getString("display_name"),
                        username)
                .stream()
                .findFirst()
                .orElse(username);
    }

    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        String currentHash = jdbc.query(
                        "SELECT password_hash FROM admin_users WHERE username=? AND enabled=TRUE",
                        (rs, rowNum) -> rs.getString("password_hash"),
                        username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("管理员账号不存在或已停用"));
        if (!passwordEncoder.matches(currentPassword, currentHash)) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        if (passwordEncoder.matches(newPassword, currentHash)) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }
        jdbc.update(
                "UPDATE admin_users SET password_hash=?,updated_at=CURRENT_TIMESTAMP WHERE username=?",
                passwordEncoder.encode(newPassword),
                username);
    }
}
