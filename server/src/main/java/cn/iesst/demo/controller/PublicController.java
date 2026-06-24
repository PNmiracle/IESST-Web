package cn.iesst.demo.controller;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.ConsultationRecord;
import cn.iesst.demo.model.ConsultationRequest;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.ManuscriptUploadReceipt;
import cn.iesst.demo.model.SubmissionReceipt;
import cn.iesst.demo.service.DemoStore;
import cn.iesst.demo.service.ManuscriptStorageService;
import cn.iesst.demo.service.SubmissionUploadTokenService;
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
    private final ManuscriptStorageService manuscriptStorageService;
    private final SubmissionUploadTokenService uploadTokenService;

    public PublicController(
            DemoStore store,
            StudentUserService studentUserService,
            ManuscriptStorageService manuscriptStorageService,
            SubmissionUploadTokenService uploadTokenService) {
        this.store = store;
        this.studentUserService = studentUserService;
        this.manuscriptStorageService = manuscriptStorageService;
        this.uploadTokenService = uploadTokenService;
    }

    @GetMapping("/banners")
    public List<Banner> banners() {
        return store.publicBanners();
    }

    @GetMapping("/journals")
    public List<Journal> journals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String deadline,
            @RequestParam(required = false) String journalLevel,
            @RequestParam(required = false) String casZone,
            @RequestParam(required = false) String jcrQuartile,
            @RequestParam(required = false) Double impactMin,
            @RequestParam(required = false) Double impactMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        return store.publicJournals(type, discipline, deadline, journalLevel, casZone, jcrQuartile, impactMin, impactMax, keyword, sort);
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
    public SubmissionReceipt submit(@Valid @RequestBody Submission submission, HttpSession session) {
        Submission saved = store.createSubmission(submission);
        studentUserService.createOrderFromSubmissionIfLoggedIn(session, saved);
        String uploadToken = uploadTokenService.issue(saved.id());
        return new SubmissionReceipt(saved.id(), saved.status(), saved.createdAt(), uploadToken);
    }

    @PostMapping("/consultations")
    public ConsultationRecord consult(@Valid @RequestBody ConsultationRequest request, HttpSession session) {
        return studentUserService.createConsultation(session, request);
    }

    @PostMapping(value = "/submissions/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ManuscriptUploadReceipt uploadSubmissionFile(
            @PathVariable long id,
            @RequestParam String uploadToken,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        uploadTokenService.claim(id, uploadToken);
        try {
            var upload = manuscriptStorageService.save(file);
            store.attachSubmissionFile(id, upload);
            studentUserService.attachSubmissionFileToOrderIfLoggedIn(session, id, upload);
            return new ManuscriptUploadReceipt(upload.fileName(), upload.size());
        } catch (RuntimeException exception) {
            uploadTokenService.release(id);
            throw exception;
        }
    }
}
