package cn.iesst.demo.controller;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.service.DemoStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final DemoStore store;

    public PublicController(DemoStore store) {
        this.store = store;
    }

    @GetMapping("/banners")
    public List<Banner> banners() {
        return store.publicBanners();
    }

    @GetMapping("/journals")
    public List<Journal> journals() {
        return store.publicJournals();
    }

    @GetMapping("/journals/{id}")
    public Journal journal(@PathVariable long id) {
        return store.publicJournal(id);
    }

    @GetMapping("/services")
    public List<ServiceOffering> services() { return store.publicServices(); }

    @GetMapping("/experts")
    public List<Expert> experts() { return store.publicExperts(); }

    @PostMapping("/submissions")
    public Submission submit(@Valid @RequestBody Submission submission) {
        return store.createSubmission(submission);
    }
}
