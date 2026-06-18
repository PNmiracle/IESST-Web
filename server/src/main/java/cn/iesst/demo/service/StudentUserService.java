package cn.iesst.demo.service;

import cn.iesst.demo.model.InvoiceRequest;
import cn.iesst.demo.model.ConsultationRecord;
import cn.iesst.demo.model.ConsultationRequest;
import cn.iesst.demo.model.StudentAccount;
import cn.iesst.demo.model.StudentAccountInput;
import cn.iesst.demo.model.StudentOrder;
import cn.iesst.demo.model.StudentOrderProgress;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.UploadResult;
import jakarta.annotation.PostConstruct;
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
    public static final String SESSION_STUDENT_MOBILE = "STUDENT_MOBILE";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final DemoStore store;

    public StudentUserService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, DemoStore store) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.store = store;
    }

    @PostConstruct
    void ensureDemoStudents() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM student_users", Integer.class);
        if (count != null && count == 0) {
            createStudent("18800000088", "student123", "张同学");
            createStudent("18800000099", "student123", "李同学");
        }
        ensureDemoSubmission("18800000088", "张同学", "SCI 特刊论文初评与期刊匹配");
        ensureDemoSubmission("18800000099", "李同学", "EI 智能制造方向投稿咨询");
        ensureDemoOrderCenter("18800000088", "SCI 特刊论文初评与期刊匹配", "SCI", "王顾问", new BigDecimal("2980.00"), true);
        ensureDemoOrderCenter("18800000099", "EI 智能制造方向投稿咨询", "EI", "陈顾问", new BigDecimal("1280.00"), false);
    }

    public Map<String, Object> login(String mobile, String password, HttpSession session) {
        StudentCredential account = findCredentialByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("手机号或密码错误"));
        if (!account.enabled() || !passwordEncoder.matches(password, account.passwordHash())) {
            throw new IllegalArgumentException("手机号或密码错误");
        }
        session.setAttribute(SESSION_STUDENT_MOBILE, account.mobile());
        return profile(account.mobile());
    }

    public Map<String, Object> register(String mobile, String displayName, String password, String confirmPassword, HttpSession session) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (findCredentialByMobile(mobile).isPresent()) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }
        jdbc.update(
                "INSERT INTO student_users(mobile,password_hash,display_name,enabled) VALUES (?,?,?,TRUE)",
                mobile,
                passwordEncoder.encode(password),
                displayName);
        session.setAttribute(SESSION_STUDENT_MOBILE, mobile);
        return profile(mobile);
    }

    public Map<String, Object> me(HttpSession session) {
        Object mobile = session.getAttribute(SESSION_STUDENT_MOBILE);
        if (!(mobile instanceof String value)) {
            return Map.of("authenticated", false);
        }
        return profile(value);
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_STUDENT_MOBILE);
    }

    public List<StudentOrder> orders(HttpSession session) {
        Long studentId = currentStudentId(session);
        return jdbc.query(
                "SELECT * FROM student_orders WHERE student_user_id=? ORDER BY created_at DESC",
                (rs, rowNum) -> mapOrder(rs),
                studentId);
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
        Object mobile = session.getAttribute(SESSION_STUDENT_MOBILE);
        if (!(mobile instanceof String value) || value.isBlank()) {
            return Optional.empty();
        }
        StudentCredential student = findCredentialByMobile(value)
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
                submission.message());
        addProgress(orderId, "SUBMITTED", "信息已提交", "你的稿件评估信息已进入后台，顾问将进行初步判断。", "系统");
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
                null,
                null));
        Long studentId = currentStudentIdIfPresent(session);
        long id = store.insertAndReturnId(
                "INSERT INTO consultation_records(student_user_id,source_submission_id,contact_name,mobile,email,source_channel,subject,content,follow_up_status) VALUES (?,?,?,?,?,?,?,?,?)",
                studentId,
                submission.id(),
                request.contactName(),
                request.mobile(),
                request.email(),
                "WEB",
                request.subject(),
                request.content(),
                "NEW");
        return findConsultation(id);
    }

    public void attachSubmissionFileToOrderIfLoggedIn(HttpSession session, long submissionId, UploadResult upload) {
        Long studentId = currentStudentIdIfPresent(session);
        if (studentId == null) {
            return;
        }
        List<Long> orderIds = jdbc.query(
                "SELECT id FROM student_orders WHERE student_user_id=? AND source_submission_id=?",
                (rs, rowNum) -> rs.getLong("id"),
                studentId,
                submissionId);
        orderIds.forEach(orderId -> store.insertAndReturnId(
                "INSERT INTO student_order_files(order_id,file_category,file_name,file_url,visible_to_student,uploaded_by) VALUES (?,?,?,?,TRUE,?)",
                orderId,
                "MANUSCRIPT",
                upload.fileName(),
                upload.url(),
                "学生"));
    }

    public List<StudentOrder> adminOrders() {
        return jdbc.query("SELECT * FROM student_orders ORDER BY created_at DESC", (rs, rowNum) -> mapOrder(rs));
    }

    public List<InvoiceRequest> adminInvoices() {
        return jdbc.query("SELECT * FROM invoice_requests ORDER BY created_at DESC", (rs, rowNum) -> mapInvoice(rs));
    }

    public StudentOrder updateOrderStatus(long id, String status) {
        String currentStage = switch (status) {
            case "NEW" -> "待顾问确认";
            case "IN_PROGRESS" -> "服务进行中";
            case "COMPLETED" -> "服务已完成";
            case "CANCELLED" -> "订单已取消";
            default -> throw new IllegalArgumentException("不支持的订单状态");
        };
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
                "SELECT id,mobile,display_name,enabled FROM student_users ORDER BY id DESC",
                (rs, rowNum) -> new StudentAccount(
                        rs.getLong("id"),
                        rs.getString("mobile"),
                        rs.getString("display_name"),
                        rs.getBoolean("enabled")));
    }

    public StudentAccount save(StudentAccountInput input) {
        if (input.id() == null) {
            if (input.password() == null || input.password().isBlank()) {
                throw new IllegalArgumentException("新增学生账号时请填写密码");
            }
            jdbc.update(
                    "INSERT INTO student_users(mobile,password_hash,display_name,enabled) VALUES (?,?,?,?)",
                    input.mobile(),
                    passwordEncoder.encode(input.password()),
                    input.displayName(),
                    input.enabled());
            return students().stream().filter(item -> item.mobile().equals(input.mobile())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("学生账号创建失败"));
        }
        jdbc.update(
                "UPDATE student_users SET mobile=?,display_name=?,enabled=? WHERE id=?",
                input.mobile(),
                input.displayName(),
                input.enabled(),
                input.id());
        if (input.password() != null && !input.password().isBlank()) {
            jdbc.update(
                    "UPDATE student_users SET password_hash=? WHERE id=?",
                    passwordEncoder.encode(input.password()),
                    input.id());
        }
        return students().stream().filter(item -> item.id().equals(input.id())).findFirst()
                .orElseThrow(() -> new IllegalStateException("学生账号更新失败"));
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM student_users WHERE id=?", id);
    }

    public String currentMobile(HttpSession session) {
        Object mobile = session.getAttribute(SESSION_STUDENT_MOBILE);
        if (!(mobile instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException("学生登录已失效，请重新登录");
        }
        return value;
    }

    private void createStudent(String mobile, String password, String displayName) {
        jdbc.update(
                "INSERT INTO student_users(mobile,password_hash,display_name,enabled) VALUES (?,?,?,TRUE)",
                mobile,
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
        addProgress(orderId, "SUBMITTED", "已提交信息", "稿件基础信息已进入顾问工作台。", "系统");
        addProgress(orderId, "ASSESSING", "稿件评估中", "正在评估研究方向、服务范围与可匹配期刊。", consultantName);
        addProgress(orderId, "FOLLOW_UP", "等待沟通确认", "顾问将确认服务节点、周期与交付材料。", consultantName);
        if (createInvoice) {
            store.insertAndReturnId(
                    "INSERT INTO invoice_requests(order_id,student_user_id,invoice_title,tax_number,invoice_type,invoice_amount,receiver_email,receiver_phone,receiver_address,status,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    orderId,
                    student.id(),
                    "张同学科研服务费",
                    "91410000DEMO12345X",
                    "ELECTRONIC",
                    amount,
                    mobile + "@example.com",
                    mobile,
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

    private Map<String, Object> profile(String mobile) {
        return findCredentialByMobile(mobile)
                .map(account -> Map.<String, Object>of(
                        "authenticated", true,
                        "mobile", account.mobile(),
                        "displayName", account.displayName()))
                .orElse(Map.<String, Object>of("authenticated", false));
    }

    private Optional<StudentCredential> findCredentialByMobile(String mobile) {
        return jdbc.query(
                        "SELECT id,mobile,password_hash,display_name,enabled FROM student_users WHERE mobile=?",
                        (rs, rowNum) -> new StudentCredential(
                                rs.getLong("id"),
                                rs.getString("mobile"),
                                rs.getString("password_hash"),
                                rs.getString("display_name"),
                                rs.getBoolean("enabled")),
                        mobile)
                .stream()
                .findFirst();
    }

    private Long currentStudentId(HttpSession session) {
        String mobile = currentMobile(session);
        return findCredentialByMobile(mobile)
                .map(StudentCredential::id)
                .orElseThrow(() -> new IllegalArgumentException("学生账号不存在，请重新登录"));
    }

    private Long currentStudentIdIfPresent(HttpSession session) {
        Object mobile = session.getAttribute(SESSION_STUDENT_MOBILE);
        if (!(mobile instanceof String value) || value.isBlank()) {
            return null;
        }
        return findCredentialByMobile(value).map(StudentCredential::id).orElse(null);
    }

    private ConsultationRecord findConsultation(long id) {
        return jdbc.query(
                        "SELECT * FROM consultation_records WHERE id=?",
                        (rs, rowNum) -> new ConsultationRecord(
                                rs.getLong("id"),
                                nullableLong(rs, "student_user_id"),
                                nullableLong(rs, "source_submission_id"),
                                rs.getString("contact_name"),
                                rs.getString("mobile"),
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
                rs.getString("receiver_phone"),
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

    private LocalDateTime timestamp(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private record StudentCredential(Long id, String mobile, String passwordHash, String displayName, boolean enabled) {
    }
}
