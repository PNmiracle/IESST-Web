package cn.iesst.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String uploadDir;
    private final AuditLogInterceptor auditLogInterceptor;
    private final SensitiveResponseHeaderInterceptor sensitiveResponseHeaderInterceptor;

    public WebConfig(
            @org.springframework.beans.factory.annotation.Value("${app.upload-dir:./uploads}") String uploadDir,
            AuditLogInterceptor auditLogInterceptor,
            SensitiveResponseHeaderInterceptor sensitiveResponseHeaderInterceptor) {
        this.uploadDir = uploadDir;
        this.auditLogInterceptor = auditLogInterceptor;
        this.sensitiveResponseHeaderInterceptor = sensitiveResponseHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditLogInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(sensitiveResponseHeaderInterceptor)
                .addPathPatterns(
                        "/api/student/**",
                        "/api/admin/students/**",
                        "/api/admin/submissions/*/files/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = java.nio.file.Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(location + "images/");
        registry.addResourceHandler("/uploads/documents/**")
                .addResourceLocations(location + "documents/");
    }
}
