package cn.iesst.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentContentInitializer {
    private final DemoStore store;

    public DevelopmentContentInitializer(DemoStore store) {
        this.store = store;
    }

    @PostConstruct
    void initialize() {
        store.seedContent();
    }
}
