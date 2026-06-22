package cn.iesst.demo.config;

import cn.iesst.demo.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.util.Set;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final AuditLogService auditLogService;

    public AuditLogInterceptor(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        String path = request.getRequestURI();
        boolean sensitiveRead = "GET".equals(request.getMethod())
                && (path.equals("/api/admin/students")
                || path.matches("/api/admin/submissions/\\d+/files(?:/\\d+/download)?"));
        if (!MUTATING_METHODS.contains(request.getMethod()) && !sensitiveRead) return;
        Principal principal = request.getUserPrincipal();
        if (principal == null) return;
        auditLogService.record(
                principal.getName(),
                sensitiveRead ? "READ_SENSITIVE" : request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                clientIp(request));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
