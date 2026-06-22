package cn.iesst.demo.controller;

import cn.iesst.demo.model.LoginRequest;
import cn.iesst.demo.model.InvoiceRequest;
import cn.iesst.demo.model.StudentRegisterRequest;
import cn.iesst.demo.model.StudentOrder;
import cn.iesst.demo.model.StudentOrderProgress;
import cn.iesst.demo.model.PageResponse;
import cn.iesst.demo.model.StoredFileInfo;
import cn.iesst.demo.service.ManuscriptStorageService;
import cn.iesst.demo.service.PrivateDownloadResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import cn.iesst.demo.service.StudentUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentUserService studentUserService;
    private final ManuscriptStorageService manuscriptStorageService;
    private final PrivateDownloadResponseFactory downloadResponseFactory;

    public StudentController(
            StudentUserService studentUserService,
            ManuscriptStorageService manuscriptStorageService,
            PrivateDownloadResponseFactory downloadResponseFactory) {
        this.studentUserService = studentUserService;
        this.manuscriptStorageService = manuscriptStorageService;
        this.downloadResponseFactory = downloadResponseFactory;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        return studentUserService.me(session);
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        Map<String, Object> profile = studentUserService.login(
                request.username(),
                request.password(),
                servletRequest.getSession());
        servletRequest.changeSessionId();
        return profile;
    }

    @PostMapping("/register")
    public Map<String, Object> register(
            @Valid @RequestBody StudentRegisterRequest request,
            HttpServletRequest servletRequest) {
        Map<String, Object> profile = studentUserService.register(
                request.mobile(),
                request.displayName(),
                request.password(),
                request.confirmPassword(),
                servletRequest.getSession());
        servletRequest.changeSessionId();
        return profile;
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(HttpSession session) {
        studentUserService.logout(session);
        return Map.of("success", true);
    }

    @GetMapping("/orders")
    public PageResponse<StudentOrder> orders(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            HttpSession session) {
        return studentUserService.orders(session, status, page, size);
    }

    @GetMapping("/orders/{id}/progress")
    public List<StudentOrderProgress> orderProgress(@PathVariable long id, HttpSession session) {
        return studentUserService.orderProgress(id, session);
    }

    @GetMapping("/orders/{id}/files")
    public List<StoredFileInfo> orderFiles(@PathVariable long id, HttpSession session) {
        return studentUserService.orderFiles(id, session);
    }

    @GetMapping("/orders/{orderId}/files/{fileId}/download")
    public ResponseEntity<?> downloadOrderFile(
            @PathVariable long orderId,
            @PathVariable long fileId,
            HttpSession session) {
        var file = studentUserService.orderFile(orderId, fileId, session);
        return downloadResponseFactory.create(manuscriptStorageService.download(
                file.storageKey(), file.fileName(), file.contentType()));
    }

    @GetMapping("/invoices")
    public List<InvoiceRequest> invoices(HttpSession session) {
        return studentUserService.invoices(session);
    }
}
