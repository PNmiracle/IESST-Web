package cn.iesst.demo.controller;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.AuditLog;
import cn.iesst.demo.model.ChangePasswordRequest;
import cn.iesst.demo.model.DashboardSummary;
import cn.iesst.demo.model.InvoiceRequest;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.PageResponse;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.SimpleStatusRequest;
import cn.iesst.demo.model.StatusRequest;
import cn.iesst.demo.model.StudentAccount;
import cn.iesst.demo.model.StudentEnabledRequest;
import cn.iesst.demo.model.StudentOrder;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.StoredFileInfo;
import cn.iesst.demo.service.ManuscriptStorageService;
import cn.iesst.demo.service.PrivateDownloadResponseFactory;
import cn.iesst.demo.service.DemoStore;
import cn.iesst.demo.service.AdminUserService;
import cn.iesst.demo.service.AuditLogService;
import cn.iesst.demo.service.StudentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final DemoStore store;
    private final AdminUserService adminUserService;
    private final AuditLogService auditLogService;
    private final StudentUserService studentUserService;
    private final ManuscriptStorageService manuscriptStorageService;
    private final PrivateDownloadResponseFactory downloadResponseFactory;

    public AdminController(
            DemoStore store,
            AdminUserService adminUserService,
            AuditLogService auditLogService,
            StudentUserService studentUserService,
            ManuscriptStorageService manuscriptStorageService,
            PrivateDownloadResponseFactory downloadResponseFactory) {
        this.store = store;
        this.adminUserService = adminUserService;
        this.auditLogService = auditLogService;
        this.studentUserService = studentUserService;
        this.manuscriptStorageService = manuscriptStorageService;
        this.downloadResponseFactory = downloadResponseFactory;
    }

    @PutMapping("/account/password")
    public Map<String, Boolean> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        adminUserService.changePassword(
                principal.getName(),
                request.currentPassword(),
                request.newPassword(),
                request.confirmPassword());
        return Map.of("success", true);
    }

    @GetMapping("/audit-logs")
    public PageResponse<AuditLog> auditLogs(
            @RequestParam(defaultValue = "全部") String action,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditLogService.search(action, keyword, page, size);
    }

    @GetMapping("/dashboard")
    public DashboardSummary dashboard() {
        return store.dashboardSummary();
    }

    @GetMapping("/banners")
    public List<Banner> banners() {
        return store.banners();
    }

    @PostMapping("/banners")
    public Banner createBanner(@Valid @RequestBody Banner banner) {
        return store.saveBanner(banner);
    }

    @PutMapping("/banners/{id}")
    public Banner updateBanner(@PathVariable long id, @Valid @RequestBody Banner banner) {
        return store.saveBanner(new Banner(id, banner.title(), banner.imageUrl(), banner.linkUrl(), banner.sortOrder(), banner.enabled()));
    }

    @DeleteMapping("/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(@PathVariable long id) {
        store.deleteBanner(id);
    }

    @GetMapping("/journals")
    public List<Journal> journals() {
        return store.journals();
    }

    @PostMapping("/journals")
    public Journal createJournal(@Valid @RequestBody Journal journal) {
        return store.saveJournal(journal);
    }

    @PutMapping("/journals/{id}")
    public Journal updateJournal(@PathVariable long id, @Valid @RequestBody Journal journal) {
        return store.saveJournal(new Journal(id, journal.type(), journal.title(), journal.field(), journal.indexType(), journal.cycle(), journal.description(), journal.imageUrl(), journal.journalLevel(), journal.impactFactorLabel(), journal.impactFactorValue(), journal.journalPartition(), journal.acceptanceTime(), journal.submissionDeadlineText(), journal.submissionDeadlineDate(), journal.disciplineCategory(), journal.casZone(), journal.jcrQuartile(), journal.viewCount(), journal.documentName(), journal.documentUrl(), journal.published()));
    }

    @DeleteMapping("/journals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJournal(@PathVariable long id) {
        store.deleteJournal(id);
    }

    @GetMapping("/submissions")
    public PageResponse<Submission> submissions(
            @RequestParam(defaultValue = "全部") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return store.submissions(status, keyword, page, size);
    }

    @GetMapping("/submissions/{id}/files")
    public List<StoredFileInfo> submissionFiles(@PathVariable long id) {
        return store.submissionFiles(id);
    }

    @GetMapping("/submissions/{submissionId}/files/{fileId}/download")
    public ResponseEntity<?> downloadSubmissionFile(
            @PathVariable long submissionId,
            @PathVariable long fileId) {
        var file = store.submissionFile(submissionId, fileId);
        return downloadResponseFactory.create(manuscriptStorageService.download(
                file.storageKey(), file.fileName(), file.contentType()));
    }

    @GetMapping(value = "/submissions/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportSubmissions(
            @RequestParam(defaultValue = "全部") String status,
            @RequestParam(defaultValue = "") String keyword) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("ID,作者,邮箱,手机号,论文标题,目标类型,服务类型,是否加急,是否参加优秀作者扶持计划,特殊要求,状态,提交时间\n");
        store.submissionsForExport(status, keyword).forEach(item -> csv
                .append(item.id()).append(',')
                .append(csvCell(item.authorName())).append(',')
                .append(csvCell(item.email())).append(',')
                .append(csvCell(item.contactPhone())).append(',')
                .append(csvCell(item.paperTitle())).append(',')
                .append(csvCell(item.targetType())).append(',')
                .append(csvCell(item.serviceType())).append(',')
                .append(csvCell(Boolean.TRUE.equals(item.expedited()) ? "是" : "否")).append(',')
                .append(csvCell(Boolean.TRUE.equals(item.authorSupportProgram()) ? "是" : "否")).append(',')
                .append(csvCell(item.specialRequirements())).append(',')
                .append(csvCell(item.status())).append(',')
                .append(csvCell(item.createdAt() == null ? "" : item.createdAt().toString()))
                .append('\n'));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submissions.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv.toString());
    }

    @PutMapping("/submissions/{id}/status")
    @Transactional
    public Submission updateStatus(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        Submission submission = store.updateSubmissionStatus(id, request.status());
        String orderStatus = switch (request.status()) {
            case "待处理" -> "NEW";
            case "沟通中" -> "IN_PROGRESS";
            case "已完成" -> "COMPLETED";
            default -> throw new IllegalArgumentException("处理状态不正确");
        };
        studentUserService.updateOrderStatusBySubmission(id, orderStatus);
        return submission;
    }

    @GetMapping("/services")
    public List<ServiceOffering> services() { return store.services(); }
    @PostMapping("/services")
    public ServiceOffering createService(@RequestBody ServiceOffering item) { return store.saveService(item); }
    @PutMapping("/services/{id}")
    public ServiceOffering updateService(@PathVariable long id, @RequestBody ServiceOffering item) { return store.saveService(new ServiceOffering(id, item.category(), item.title(), item.price(), item.description(), item.features(), item.published())); }
    @DeleteMapping("/services/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable long id) { store.deleteService(id); }

    @GetMapping("/experts")
    public List<Expert> experts() { return store.experts(); }
    @PostMapping("/experts")
    public Expert createExpert(@RequestBody Expert item) { return store.saveExpert(item); }
    @PutMapping("/experts/{id}")
    public Expert updateExpert(@PathVariable long id, @RequestBody Expert item) { return store.saveExpert(new Expert(id, item.name(), item.institution(), item.role(), item.imageUrl(), item.published())); }
    @DeleteMapping("/experts/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpert(@PathVariable long id) { store.deleteExpert(id); }

    @GetMapping("/students")
    public List<StudentAccount> students() {
        return studentUserService.students();
    }

    @PutMapping("/students/{id}/enabled")
    public StudentAccount setStudentEnabled(
            @PathVariable long id,
            @RequestBody StudentEnabledRequest request) {
        return studentUserService.setEnabled(id, request.enabled());
    }

    @GetMapping("/orders")
    public List<StudentOrder> orders() {
        return studentUserService.adminOrders();
    }

    @PutMapping("/orders/{id}/status")
    @Transactional
    public StudentOrder updateOrderStatus(@PathVariable long id, @Valid @RequestBody SimpleStatusRequest request) {
        StudentOrder order = studentUserService.updateOrderStatus(id, request.status());
        if (order.sourceSubmissionId() != null && !"CANCELLED".equals(order.orderStatus())) {
            String submissionStatus = switch (order.orderStatus()) {
                case "NEW" -> "待处理";
                case "IN_PROGRESS" -> "沟通中";
                case "COMPLETED" -> "已完成";
                default -> throw new IllegalArgumentException("不支持的订单状态");
            };
            store.updateSubmissionStatus(order.sourceSubmissionId(), submissionStatus);
        }
        return order;
    }

    @GetMapping("/invoices")
    public List<InvoiceRequest> invoices() {
        return studentUserService.adminInvoices();
    }

    @PutMapping("/invoices/{id}/status")
    public InvoiceRequest updateInvoiceStatus(@PathVariable long id, @Valid @RequestBody SimpleStatusRequest request) {
        return studentUserService.updateInvoiceStatus(id, request.status());
    }

    private String csvCell(Object value) {
        if (value == null) return "\"\"";
        return "\"" + value.toString().replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }
}
