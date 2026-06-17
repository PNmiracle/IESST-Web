package cn.iesst.demo.service;

import cn.iesst.demo.model.AuditLog;
import cn.iesst.demo.model.PageResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {
    private final JdbcTemplate jdbc;

    public AuditLogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void record(String username, String action, String resourcePath, int responseStatus, String ipAddress) {
        jdbc.update(
                "INSERT INTO audit_logs(username,action,resource_path,response_status,ip_address) VALUES (?,?,?,?,?)",
                username, action, resourcePath, responseStatus, ipAddress);
    }

    public List<AuditLog> recent(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return jdbc.query(
                "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> new AuditLog(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("resource_path"),
                        rs.getInt("response_status"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("created_at").toLocalDateTime()),
                safeLimit);
    }

    public PageResponse<AuditLog> search(String action, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> arguments = new ArrayList<>();

        if (action != null && !action.isBlank() && !"全部".equals(action)) {
            where.append(" AND action=?");
            arguments.add(action);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (username LIKE ? OR resource_path LIKE ? OR ip_address LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            arguments.add(pattern);
            arguments.add(pattern);
            arguments.add(pattern);
        }

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_logs" + where,
                Long.class,
                arguments.toArray());
        List<Object> pageArguments = new ArrayList<>(arguments);
        pageArguments.add(safeSize);
        pageArguments.add((safePage - 1) * safeSize);
        List<AuditLog> items = jdbc.query(
                "SELECT * FROM audit_logs" + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> new AuditLog(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("resource_path"),
                        rs.getInt("response_status"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("created_at").toLocalDateTime()),
                pageArguments.toArray());
        return PageResponse.of(items, safePage, safeSize, total == null ? 0 : total);
    }
}
