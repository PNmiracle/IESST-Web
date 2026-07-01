package cn.iesst.demo.service;

import cn.iesst.demo.model.InvoiceRequest;
import cn.iesst.demo.model.ConsultationRecord;
import cn.iesst.demo.model.ConsultationRequest;
import cn.iesst.demo.model.StudentAccount;
import cn.iesst.demo.model.StudentOrder;
import cn.iesst.demo.model.StudentOrderProgress;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.PageResponse;
import cn.iesst.demo.model.StoredFileInfo;
import cn.iesst.demo.service.ManuscriptStorageService.StoredManuscript;
import cn.iesst.demo.security.PiiCryptoService;
import cn.iesst.demo.security.StudentAuthenticationException;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentUserService {
    public static final String SESSION_STUDENT_ID = "STUDENT_ID";
    private static final int MAX_FAILED_LOGINS = 5;
    private static final int LOCK_MINUTES = 15;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final DemoStore store;
    private final PiiCryptoService piiCrypto;

    public StudentUserService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, DemoStore store, PiiCryptoService piiCrypto) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.store = store;
        this.piiCrypto = piiCrypto;
    }

    void initializeDemoData() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM student_users", Integer.class);
        if (count != null && count == 0) {
            createStudent("18800000088", "student123", "张同学");
            createStudent("18800000099", "student123", "李同学");
        }
        ensureDemoSubmission("18800000088", "张同学", "SCI 特刊论文初评与期刊匹配");
        ensureDemoSubmission("18800000099", "李同学", "EI 智能制造方向投稿咨询");
        ensureDemoOrderCenter("18800000088", "SCI 特刊论文初评与期刊匹配", "SCI", "王编辑", new BigDecimal("2980.00"), true);
        ensureDemoOrderCenter("18800000099", "EI 智能制造方向投稿咨询", "EI", "陈编辑", new BigDecimal("1280.00"), false);
        reconcileLinkedSubmissionStatuses();
    }

    public Map<String, Object> login(String mobile, String password, HttpSession session) {
        StudentCredential account = findCredentialByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("手机号或密码错误"));
        if (account.lockedUntil() != null && account.lockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("登录失败次数过多，请15分钟后重试");
        }
        if (!account.enabled()) {
            throw new IllegalArgumentException("手机号或密码错误");
        }
        if (!passwordEncoder.matches(password, account.passwordHash())) {
            recordFailedLogin(account);
            throw new IllegalArgumentException(account.failedLoginAttempts() + 1 >= MAX_FAILED_LOGINS
                    ? "登录失败次数过多，请15分钟后重试"
                    : "手机号或密码错误");
        }
        jdbc.update(
                "UPDATE student_users SET failed_login_attempts=0,locked_until=NULL,last_login_at=CURRENT_TIMESTAMP WHERE id=?",
                account.id());
        session.setAttribute(SESSION_STUDENT_ID, account.id());
        return profile(account.id());
    }

    public Map<String, Object> register(String mobile, String displayName, String password, String confirmPassword, HttpSession session) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        String normalizedMobile = piiCrypto.normalizeMobile(mobile);
        if (findCredentialByMobile(normalizedMobile).isPresent()) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }
        jdbc.update(
                "INSERT INTO student_users(mobile_ciphertext,mobile_lookup_hash,mobile_last4,password_hash,display_name,enabled) VALUES (?,?,?,?,?,TRUE)",
                piiCrypto.encryptMobile(normalizedMobile),
                piiCrypto.mobileLookupHash(normalizedMobile),
                piiCrypto.mobileLastFour(normalizedMobile),
                passwordEncoder.encode(password),
                displayName);
        StudentCredential account = findCredentialByMobile(normalizedMobile)
                .orElseThrow(() -> new IllegalStateException("学生账号创建失败"));
        session.setAttribute(SESSION_STUDENT_ID, account.id());
        return profile(account.id());
    }

    public Map<String, Object> me(HttpSession session) {
        Object studentId = session.getAttribute(SESSION_STUDENT_ID);
        if (!(studentId instanceof Long value)) {
            return Map.of("authenticated", false);
        }
        Optional<StudentCredential> account = findCredentialById(value);
        if (account.isEmpty() || !account.get().enabled()) {
            session.removeAttribute(SESSION_STUDENT_ID);
            return Map.of("authenticated", false);
        }
        return profile(value);
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_STUDENT_ID);
    }

    public PageResponse<StudentOrder> orders(HttpSession session, String status, int page, int size) {
        Long studentId = currentStudentId(session);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 20);
        boolean filtered = status != null && !status.isBlank() && !"ALL".equals(status);
        String where = filtered ? " WHERE student_user_id=? AND order_status=?" : " WHERE student_user_id=?";
        List<Object> args = new java.util.ArrayList<>();
        args.add(studentId);
        if (filtered) {
            args.add(status);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM student_orders" + where, Long.class, args.toArray());
        args.add(safeSize);
        args.add((safePage - 1) * safeSize);
        List<StudentOrder> items = jdbc.query(
                "SELECT * FROM student_orders" + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapOrder(rs),
                args.toArray());
        return PageResponse.of(items, safePage, safeSize, total == null ? 0 : total);
    }

    public List<StudentOrderProgress> orderProgress(long orderId, HttpSession session) {
        Long studentId = currentStudentId(session);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student_orders WHERE id=? AND student_user_id=?",
                Integer.class,
                orderId,
                studentId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("订单不存在或无权查看");
        }
        return jdbc.query(
                "SELECT * FROM student_order_progress WHERE order_id=? AND visible_to_student=TRUE ORDER BY created_at ASC, id ASC",
                (rs, rowNum) -> mapOrderProgress(rs),
                orderId);
    }

    public List<InvoiceRequest> invoices(HttpSession session) {
        Long studentId = currentStudentId(session);
        return jdbc.query(
                "SELECT * FROM invoice_requests WHERE student_user_id=? ORDER BY created_at DESC",
                (rs, rowNum) -> mapInvoice(rs),
                studentId);
    }

    public Optional<StudentOrder> createOrderFromSubmissionIfLoggedIn(HttpSession session, Submission submission) {
        Long studentId = currentStudentIdIfPresent(session);
        if (studentId == null) {
            return Optional.empty();
        }
        StudentCredential student = findCredentialById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生账号不存在，请重新登录"));
        String targetType = submission.targetType() == null || submission.targetType().isBlank()
                ? "咨询"
                : submission.targetType();
        String serviceCategory = switch (targetType) {
            case "翻译润色" -> "translation";
            case "科学编辑" -> "editing";
            default -> "publication";
        };
        String orderNo = "IESST-SUB-" + submission.id();
        long orderId = store.insertAndReturnId(
                "INSERT INTO student_orders(order_no,student_user_id,source_submission_id,service_category,target_type,title,current_stage,amount,currency_code,payment_status,order_status,consultant_name,notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                orderNo,
                student.id(),
                submission.id(),
                serviceCategory,
                targetType,
                submission.paperTitle(),
                "信息已提交",
                BigDecimal.ZERO,
                "CNY",
                "UNPAID",
                "NEW",
                null,
                submission.specialRequirements() == null || submission.specialRequirements().isBlank()
                        ? submission.message()
                        : submission.specialRequirements());
        addProgress(orderId, "SUBMITTED", "信息已提交", "你的稿件评估信息已进入后台，编辑将进行初步判断。", "系统");
        return adminOrders().stream()
                .filter(item -> item.id().equals(orderId))
                .findFirst();
    }

    public ConsultationRecord createConsultation(HttpSession session, ConsultationRequest request) {
        Submission submission = store.createSubmission(new Submission(
                null,
                request.contactName(),
                request.email(),
                request.subject(),
                request.targetType(),
                request.content(),
                "咨询编辑",
                false,
                request.mobile(),
                null,
                List.of(),
                null,
                null));
        Long studentId = currentStudentIdIfPresent(session);
        long id = store.insertAndReturnId(
                "INSERT INTO consultation_records(student_user_id,source_submission_id,contact_name,mobile_ciphertext,email,source_channel,subject,content,follow_up_status) VALUES (?,?,?,?,?,?,?,?,?)",
                studentId,
                submission.id(),
                request.contactName(),
                encryptOptionalMobile(request.mobile()),
                request.email(),
                "WEB",
                request.subject(),
                request.content(),
                "NEW");
        return findConsultation(id);
    }

    public void attachSubmissionFileToOrder(long submissionId, StoredManuscript upload) {
        List<Long> orderIds = jdbc.query(
                "SELECT id FROM student_orders WHERE source_submission_id=?",
                (rs, rowNum) -> rs.getLong("id"),
                submissionId);
        orderIds.forEach(orderId -> store.insertAndReturnId(
                "INSERT INTO student_order_files(order_id,file_category,file_name,storage_key,file_size,content_type,visible_to_student,uploaded_by) VALUES (?,?,?,?,?,?,TRUE,?)",
                orderId,
                "MANUSCRIPT",
                upload.fileName(),
                upload.storageKey(),
                upload.size(),
                upload.contentType(),
                "学生"));
    }

    public List<StoredFileInfo> orderFiles(long orderId, HttpSession session) {
        Long studentId = currentStudentId(session);
        requireOwnedOrder(orderId, studentId);
        return jdbc.query(
                "SELECT f.id,f.file_name,f.file_size,f.content_type,f.created_at FROM student_order_files f " +
                        "JOIN student_orders o ON o.id=f.order_id WHERE f.order_id=? AND o.student_user_id=? AND f.visible_to_student=TRUE ORDER BY f.created_at DESC",
                (rs, rowNum) -> new StoredFileInfo(
                        rs.getLong("id"),
                        rs.getString("file_name"),
                        rs.getLong("file_size"),
                        rs.getString("content_type"),
                        rs.getTimestamp("created_at").toLocalDateTime()),
                orderId,
                studentId);
    }

    public StoredFileRecord orderFile(long orderId, long fileId, HttpSession session) {
        Long studentId = currentStudentId(session);
        requireOwnedOrder(orderId, studentId);
        return jdbc.query(
                        "SELECT f.id,f.file_name,f.storage_key,f.file_size,f.content_type,f.created_at FROM student_order_files f " +
                                "JOIN student_orders o ON o.id=f.order_id WHERE f.order_id=? AND f.id=? AND o.student_user_id=? AND f.visible_to_student=TRUE",
                        (rs, rowNum) -> new StoredFileRecord(
                                rs.getLong("id"),
                                rs.getString("file_name"),
                                rs.getString("storage_key"),
                                rs.getLong("file_size"),
                                rs.getString("content_type"),
                                rs.getTimestamp("created_at").toLocalDateTime()),
                        orderId,
                        fileId,
                        studentId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("订单附件不存在或无权查看"));
    }

    private void requireOwnedOrder(long orderId, long studentId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student_orders WHERE id=? AND student_user_id=?",
                Integer.class,
                orderId,
                studentId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("订单不存在或无权查看");
        }
    }

    public List<StudentOrder> adminOrders() {
        return jdbc.query("SELECT * FROM student_orders ORDER BY created_at DESC", (rs, rowNum) -> mapOrder(rs));
    }

    public List<InvoiceRequest> adminInvoices() {
        return jdbc.query("SELECT * FROM invoice_requests ORDER BY created_at DESC", (rs, rowNum) -> mapInvoice(rs));
    }

    public StudentOrder updateOrderStatus(long id, String status) {
        String currentStage = switch (status) {
            case "NEW" -> "待编辑确认";
            case "IN_PROGRESS" -> "服务进行中";
            case "COMPLETED" -> "服务已完成";
            case "CANCELLED" -> "订单已取消";
            default -> throw new IllegalArgumentException("不支持的订单状态");
        };
        StudentOrder current = findOrder(id);
        if (status.equals(current.orderStatus())) {
            return current;
        }
        int updated = jdbc.update(
                "UPDATE student_orders SET order_status=?,current_stage=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                status,
                currentStage,
                id);
        if (updated == 0) {
            throw new IllegalArgumentException("订单不存在");
        }
        addProgress(id, status, currentStage, "管理员已更新订单状态。", "后台管理员");
        return adminOrders().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }

    public Optional<StudentOrder> updateOrderStatusBySubmission(long submissionId, String status) {
        return jdbc.query(
                        "SELECT id FROM student_orders WHERE source_submission_id=?",
                        (rs, rowNum) -> rs.getLong("id"),
                        submissionId)
                .stream()
                .findFirst()
                .map(orderId -> updateOrderStatus(orderId, status));
    }

    public InvoiceRequest updateInvoiceStatus(long id, String status) {
        if (!List.of("PENDING", "ISSUED", "REJECTED").contains(status)) {
            throw new IllegalArgumentException("不支持的发票状态");
        }
        int updated = jdbc.update(
                "UPDATE invoice_requests SET status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                status,
                id);
        if (updated == 0) {
            throw new IllegalArgumentException("发票记录不存在");
        }
        return adminInvoices().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("发票记录不存在"));
    }

    public List<StudentAccount> students() {
        return jdbc.query(
                "SELECT id,mobile_ciphertext,display_name,enabled FROM student_users ORDER BY id DESC",
                (rs, rowNum) -> new StudentAccount(
                        rs.getLong("id"),
                        piiCrypto.decryptMobile(rs.getString("mobile_ciphertext")),
                        rs.getString("display_name"),
                        rs.getBoolean("enabled")));
    }

    public StudentAccount setEnabled(long id, boolean enabled) {
        int updated = jdbc.update(
                "UPDATE student_users SET enabled=?,failed_login_attempts=0,locked_until=NULL WHERE id=?",
                enabled,
                id);
        if (updated == 0) {
            throw new IllegalArgumentException("学生账号不存在");
        }
        return students().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("学生账号状态更新失败"));
    }

    private void createStudent(String mobile, String password, String displayName) {
        String normalizedMobile = piiCrypto.normalizeMobile(mobile);
        jdbc.update(
                "INSERT INTO student_users(mobile_ciphertext,mobile_lookup_hash,mobile_last4,password_hash,display_name,enabled) VALUES (?,?,?,?,?,TRUE)",
                piiCrypto.encryptMobile(normalizedMobile),
                piiCrypto.mobileLookupHash(normalizedMobile),
                piiCrypto.mobileLastFour(normalizedMobile),
                passwordEncoder.encode(password),
                displayName);
    }

    private void ensureDemoSubmission(String mobile, String authorName, String paperTitle) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM submissions WHERE email=?",
                Integer.class,
                mobile);
        if (count != null && count == 0) {
            store.createSubmission(new Submission(
                    null,
                    authorName,
                    mobile,
                    paperTitle,
                    paperTitle.contains("EI") ? "EI" : "SCI",
                    "学生端演示订单，账号：" + mobile,
                    null,
                    false,
                    mobile,
                    null,
                    List.of(),
                    null,
                    null));
        }
    }

    private void ensureDemoOrderCenter(
            String mobile,
            String title,
            String targetType,
            String consultantName,
            BigDecimal amount,
            boolean createInvoice) {
        StudentCredential student = findCredentialByMobile(mobile)
                .orElseThrow(() -> new IllegalStateException("演示学生账号不存在：" + mobile));
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student_orders WHERE student_user_id=? AND title=?",
                Integer.class,
                student.id(),
                title);
        if (count != null && count > 0) {
            return;
        }
        long orderId = store.insertAndReturnId(
                "INSERT INTO student_orders(order_no,student_user_id,service_category,target_type,title,current_stage,amount,currency_code,payment_status,order_status,consultant_name,notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                "IESST-" + targetType + "-" + mobile.substring(mobile.length() - 4),
                student.id(),
                "publication",
                targetType,
                title,
                "稿件评估中",
                amount,
                "CNY",
                createInvoice ? "PAID" : "UNPAID",
                "IN_PROGRESS",
                consultantName,
                "演示订单：用于学生端查看订单、进度与发票信息。");
        addProgress(orderId, "SUBMITTED", "已提交信息", "稿件基础信息已进入编辑工作台。", "系统");
        addProgress(orderId, "ASSESSING", "稿件评估中", "正在评估研究方向、服务范围与可匹配期刊。", consultantName);
        addProgress(orderId, "FOLLOW_UP", "等待沟通确认", "编辑将确认服务节点、周期与交付材料。", consultantName);
        if (createInvoice) {
            store.insertAndReturnId(
                    "INSERT INTO invoice_requests(order_id,student_user_id,invoice_title,tax_number,invoice_type,invoice_amount,receiver_email,receiver_phone_ciphertext,receiver_address,status,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    orderId,
                    student.id(),
                    "张同学科研服务费",
                    "91410000DEMO12345X",
                    "ELECTRONIC",
                    amount,
                    mobile + "@example.com",
                    piiCrypto.encryptMobile(mobile),
                    "线上交付",
                    "ISSUED",
                    "演示发票：模拟投稿后台我的发票记录。");
        }
    }

    private void addProgress(long orderId, String code, String label, String note, String operatorName) {
        store.insertAndReturnId(
                "INSERT INTO student_order_progress(order_id,stage_code,stage_label,progress_note,visible_to_student,operator_name) VALUES (?,?,?,?,TRUE,?)",
                orderId,
                code,
                label,
                note,
                operatorName);
    }

    private void reconcileLinkedSubmissionStatuses() {
        jdbc.query(
                "SELECT o.id,s.status FROM student_orders o JOIN submissions s ON s.id=o.source_submission_id",
                (rs, rowNum) -> new LinkedSubmissionStatus(rs.getLong("id"), rs.getString("status")))
                .forEach(item -> {
                    String orderStatus = switch (item.submissionStatus()) {
                        case "待处理" -> "NEW";
                        case "沟通中" -> "IN_PROGRESS";
                        case "已完成" -> "COMPLETED";
                        default -> null;
                    };
                    if (orderStatus != null) {
                        updateOrderStatus(item.orderId(), orderStatus);
                    }
                });
    }

    private Map<String, Object> profile(long studentId) {
        return findCredentialById(studentId)
                .map(account -> Map.<String, Object>of(
                        "authenticated", true,
                        "mobile", piiCrypto.decryptMobile(account.mobileCiphertext()),
                        "displayName", account.displayName()))
                .orElse(Map.<String, Object>of("authenticated", false));
    }

    private Optional<StudentCredential> findCredentialByMobile(String mobile) {
        return jdbc.query(
                        "SELECT id,mobile_ciphertext,password_hash,display_name,enabled,failed_login_attempts,locked_until FROM student_users WHERE mobile_lookup_hash=?",
                        this::mapCredential,
                        piiCrypto.mobileLookupHash(mobile))
                .stream()
                .findFirst();
    }

    private Optional<StudentCredential> findCredentialById(long id) {
        return jdbc.query(
                        "SELECT id,mobile_ciphertext,password_hash,display_name,enabled,failed_login_attempts,locked_until FROM student_users WHERE id=?",
                        this::mapCredential,
                        id)
                .stream()
                .findFirst();
    }

    private StudentCredential mapCredential(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp lockedUntil = rs.getTimestamp("locked_until");
        return new StudentCredential(
                rs.getLong("id"),
                rs.getString("mobile_ciphertext"),
                rs.getString("password_hash"),
                rs.getString("display_name"),
                rs.getBoolean("enabled"),
                rs.getInt("failed_login_attempts"),
                lockedUntil == null ? null : lockedUntil.toLocalDateTime());
    }

    private void recordFailedLogin(StudentCredential account) {
        int attempts = account.failedLoginAttempts() + 1;
        LocalDateTime lockedUntil = attempts >= MAX_FAILED_LOGINS
                ? LocalDateTime.now().plusMinutes(LOCK_MINUTES)
                : null;
        jdbc.update(
                "UPDATE student_users SET failed_login_attempts=?,locked_until=? WHERE id=?",
                attempts,
                lockedUntil == null ? null : Timestamp.valueOf(lockedUntil),
                account.id());
    }

    private Long currentStudentId(HttpSession session) {
        Object studentId = session.getAttribute(SESSION_STUDENT_ID);
        if (!(studentId instanceof Long value)) {
            throw new StudentAuthenticationException("学生登录已失效，请重新登录");
        }
        StudentCredential account = findCredentialById(value)
                .orElseThrow(() -> new StudentAuthenticationException("学生账号不存在，请重新登录"));
        if (!account.enabled()) {
            session.removeAttribute(SESSION_STUDENT_ID);
            throw new StudentAuthenticationException("学生账号已停用，请联系管理员");
        }
        return account.id();
    }

    private Long currentStudentIdIfPresent(HttpSession session) {
        Object studentId = session.getAttribute(SESSION_STUDENT_ID);
        if (!(studentId instanceof Long value)) {
            return null;
        }
        Optional<StudentCredential> account = findCredentialById(value);
        if (account.isEmpty() || !account.get().enabled()) {
            session.removeAttribute(SESSION_STUDENT_ID);
            return null;
        }
        return account
                .filter(StudentCredential::enabled)
                .map(StudentCredential::id)
                .orElse(null);
    }

    private ConsultationRecord findConsultation(long id) {
        return jdbc.query(
                        "SELECT * FROM consultation_records WHERE id=?",
                        (rs, rowNum) -> new ConsultationRecord(
                                rs.getLong("id"),
                                nullableLong(rs, "student_user_id"),
                                nullableLong(rs, "source_submission_id"),
                                rs.getString("contact_name"),
                                decryptOptionalMobile(rs.getString("mobile_ciphertext")),
                                rs.getString("email"),
                                rs.getString("source_channel"),
                                rs.getString("subject"),
                                rs.getString("content"),
                                rs.getString("consultant_name"),
                                rs.getString("follow_up_status"),
                                timestamp(rs, "created_at"),
                                timestamp(rs, "updated_at")),
                        id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("咨询记录创建失败"));
    }

    private StudentOrder mapOrder(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new StudentOrder(
                rs.getLong("id"),
                rs.getString("order_no"),
                rs.getLong("student_user_id"),
                nullableLong(rs, "source_submission_id"),
                rs.getString("service_category"),
                rs.getString("target_type"),
                rs.getString("title"),
                rs.getString("current_stage"),
                rs.getBigDecimal("amount"),
                rs.getString("currency_code"),
                rs.getString("payment_status"),
                rs.getString("order_status"),
                rs.getString("consultant_name"),
                rs.getString("notes"),
                timestamp(rs, "created_at"),
                timestamp(rs, "updated_at"));
    }

    private StudentOrder findOrder(long id) {
        return jdbc.query(
                        "SELECT * FROM student_orders WHERE id=?",
                        (rs, rowNum) -> mapOrder(rs),
                        id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }

    private StudentOrderProgress mapOrderProgress(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new StudentOrderProgress(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("stage_code"),
                rs.getString("stage_label"),
                rs.getString("progress_note"),
                rs.getBoolean("visible_to_student"),
                rs.getString("operator_name"),
                timestamp(rs, "created_at"));
    }

    private InvoiceRequest mapInvoice(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new InvoiceRequest(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getLong("student_user_id"),
                rs.getString("invoice_title"),
                rs.getString("tax_number"),
                rs.getString("invoice_type"),
                rs.getBigDecimal("invoice_amount"),
                rs.getString("receiver_email"),
                decryptOptionalMobile(rs.getString("receiver_phone_ciphertext")),
                rs.getString("receiver_address"),
                rs.getString("status"),
                rs.getString("remark"),
                timestamp(rs, "created_at"),
                timestamp(rs, "updated_at"));
    }

    private Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private String encryptOptionalMobile(String mobile) {
        return mobile == null || mobile.isBlank() ? null : piiCrypto.encryptMobile(mobile);
    }

    private String decryptOptionalMobile(String ciphertext) {
        return ciphertext == null || ciphertext.isBlank() ? null : piiCrypto.decryptMobile(ciphertext);
    }

    private LocalDateTime timestamp(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private record StudentCredential(
            Long id,
            String mobileCiphertext,
            String passwordHash,
            String displayName,
            boolean enabled,
            int failedLoginAttempts,
            LocalDateTime lockedUntil) {
    }

    private record LinkedSubmissionStatus(Long orderId, String submissionStatus) {
    }
}
