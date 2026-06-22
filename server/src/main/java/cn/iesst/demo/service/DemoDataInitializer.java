package cn.iesst.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev-demo")
public class DemoDataInitializer {
    private final StudentUserService studentUserService;

    public DemoDataInitializer(StudentUserService studentUserService) {
        this.studentUserService = studentUserService;
    }

    @PostConstruct
    void initialize() {
        studentUserService.initializeDemoData();
    }
}
