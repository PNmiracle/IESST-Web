package cn.iesst.demo.controller;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.ConsultationRecord;
import cn.iesst.demo.model.ConsultationRequest;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.UploadResult;
import cn.iesst.demo.service.DemoStore;
import cn.iesst.demo.service.FileStorageService;
import cn.iesst.demo.service.StudentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final DemoStore store;
    private final StudentUserService studentUserService;
    private final FileStorageService fileStorageService;

    public PublicController(DemoStore store, StudentUserService studentUserService, FileStorageService fileStorageService) {
        this.store = store;
        this.studentUserService = studentUserService;
        this.fileStorageService = fileStorageService;
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
    public Submission submit(@Valid @RequestBody Submission submission, HttpSession session) {
        Submission saved = store.createSubmission(submission);
        studentUserService.createOrderFromSubmissionIfLoggedIn(session, saved);
        return saved;
    }

    @PostMapping("/consultations")
    public ConsultationRecord consult(@Valid @RequestBody ConsultationRequest request, HttpSession session) {
        return studentUserService.createConsultation(session, request);
    }

    @PostMapping(value = "/submissions/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResult uploadSubmissionFile(
            @PathVariable long id,
            @RequestParam String email,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        store.validateSubmissionOwner(id, email);
        UploadResult upload = fileStorageService.saveManuscript(file);
        store.attachSubmissionFile(id, email, upload);
        studentUserService.attachSubmissionFileToOrderIfLoggedIn(session, id, upload);
        return upload;
    }
}
