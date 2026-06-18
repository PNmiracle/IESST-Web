package cn.iesst.demo.service;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.DashboardSummary;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.PageResponse;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.model.UploadResult;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class DemoStore {
    private final JdbcTemplate jdbc;

    public DemoStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    void seed() {
        if (count("banners") == 0) {
            saveBanner(new Banner(null, "思研学术 SCI 特刊交流中心", "/images/hero-center.jpg", "/SCI", 1, true));
            saveBanner(new Banner(null, "SCI 特刊快速通道", "/images/hero-fast-track.jpg", "/SCI", 2, true));
        }
        if (count("journals") == 0) {
            saveJournal(new Journal(null, "SCI", "International Journal of Mental Health Studies", "医学与健康", "SCI / SSCI", "3-6个月", "心理健康、公共卫生与临床实践方向。", null, null, true));
            saveJournal(new Journal(null, "SCI", "Journal of Intelligent Systems Engineering", "计算机与人工智能", "SCI", "4-7个月", "智能计算、机器学习与复杂工程应用。", null, null, true));
            saveJournal(new Journal(null, "EI", "Smart Manufacturing and Industrial Systems", "自动化与制造", "EI Compendex", "2-4个月", "智能制造、工业互联网与数字孪生。", null, null, true));
            saveJournal(new Journal(null, "EI", "Electronic Information and Communication", "电子与通信", "EI Compendex", "2-4个月", "电子信息、信号处理与通信网络。", null, null, false));
        }
        if (count("submissions") == 0) {
            insertSubmission(new Submission(null, "张老师", "zhang@example.com", "AI-assisted scientific writing study", "SCI", "希望评估期刊匹配方向。", "待处理", LocalDateTime.now().minusDays(1)));
            insertSubmission(new Submission(null, "李同学", "li@example.com", "Smart manufacturing digital twin", "EI", "需要了解预计发表周期。", "沟通中", LocalDateTime.now().minusHours(4)));
        }
        if (count("service_offerings") == 0) {
            saveService(new ServiceOffering(null, "translation", "高级翻译", "¥0.8/字", "由具备学科背景的双语专家完成中英翻译，并进行术语与逻辑复核。", "资深译员首轮翻译\n学科领域专家审核\n15天内一次免费修订", true));
            saveService(new ServiceOffering(null, "translation", "深度润色", "¥0.5/词", "优化语法、术语、句式和学术表达，使全文逻辑清晰、上下文自然。", "专业术语编校\n语言错误全面修正\n整体结构与表达审校", true));
            saveService(new ServiceOffering(null, "editing", "稿件诊断", "按稿评估", "从期刊适配、结构逻辑、研究亮点和发表风险等维度形成诊断建议。", "研究亮点评估\n结构与逻辑诊断\n目标期刊适配建议", true));
            saveService(new ServiceOffering(null, "editing", "科学编辑", "定制报价", "围绕标题、摘要、论证结构、图表说明和研究叙事进行深度编辑。", "学术逻辑优化\n图表与摘要建议\n编辑修改说明", true));
            saveService(new ServiceOffering(null, "editing", "返修支持", "按轮评估", "协助梳理审稿意见、制定回复策略，并优化逐条回复的科学表达。", "审稿意见分类\n回复策略建议\nResponse Letter 编辑", true));
        }
        if (count("experts") == 0) {
            saveExpert(new Expert(null, "Sos S. Agaian", "City University of New York", "学术顾问 · 编辑专家", "/images/experts/sos-agaian.jpg", true));
            saveExpert(new Expert(null, "Om P. Malik", "University of Calgary", "学术顾问 · 编辑专家", "/images/experts/om-malik.jpg", true));
            saveExpert(new Expert(null, "Maike Neuhaus", "The University of Queensland", "学术顾问 · 编辑专家", "/images/experts/maike-neuhaus.jpg", true));
            saveExpert(new Expert(null, "Jumril Yunas", "Universiti Kebangsaan Malaysia", "学术顾问 · 编辑专家", "/images/experts/jumril-yunas.jpg", true));
            saveExpert(new Expert(null, "Oussama Khatib", "Stanford University", "学术顾问 · 编辑专家", "/images/experts/oussama-khatib.jpg", true));
            saveExpert(new Expert(null, "Ali M. Eltamaly", "King Saud University", "学术顾问 · 编辑专家", "/images/experts/ali-eltamaly.jpg", true));
        }
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    public long insertAndReturnId(String sql, Object... values) {
        return insert(sql, values);
    }

    private long insert(String sql, Object... values) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            return statement;
        }, keys);
        if (keys.getKey() == null) throw new IllegalStateException("数据库未返回自增主键");
        return keys.getKey().longValue();
    }

    public List<Banner> publicBanners() {
        return jdbc.query("SELECT * FROM banners WHERE enabled=TRUE ORDER BY sort_order", (rs, n) ->
                new Banner(rs.getLong("id"), rs.getString("title"), rs.getString("image_url"), rs.getString("link_url"), rs.getInt("sort_order"), rs.getBoolean("enabled")));
    }

    public List<Banner> banners() {
        return jdbc.query("SELECT * FROM banners ORDER BY sort_order", (rs, n) ->
                new Banner(rs.getLong("id"), rs.getString("title"), rs.getString("image_url"), rs.getString("link_url"), rs.getInt("sort_order"), rs.getBoolean("enabled")));
    }

    public Banner saveBanner(Banner input) {
        if (input.id() == null) {
            long id = insert("INSERT INTO banners(title,image_url,link_url,sort_order,enabled) VALUES (?,?,?,?,?)", input.title(), input.imageUrl(), input.linkUrl(), input.sortOrder(), input.enabled());
            return new Banner(id, input.title(), input.imageUrl(), input.linkUrl(), input.sortOrder(), input.enabled());
        }
        long id = input.id();
        jdbc.update("UPDATE banners SET title=?,image_url=?,link_url=?,sort_order=?,enabled=? WHERE id=?", input.title(), input.imageUrl(), input.linkUrl(), input.sortOrder(), input.enabled(), id);
        return new Banner(id, input.title(), input.imageUrl(), input.linkUrl(), input.sortOrder(), input.enabled());
    }

    public void deleteBanner(long id) { jdbc.update("DELETE FROM banners WHERE id=?", id); }

    public List<Journal> publicJournals() { return queryJournals("SELECT * FROM journals WHERE published=TRUE ORDER BY id"); }
    public List<Journal> journals() { return queryJournals("SELECT * FROM journals ORDER BY id"); }
    public Journal publicJournal(long id) {
        return jdbc.query(
                        "SELECT * FROM journals WHERE published=TRUE AND id=?",
                        (rs, n) -> mapJournal(rs),
                        id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("期刊不存在或未上架"));
    }
    private List<Journal> queryJournals(String sql) {
        return jdbc.query(sql, (rs, n) -> mapJournal(rs));
    }
    public Journal saveJournal(Journal input) {
        if (input.id() == null) {
            long id = insert("INSERT INTO journals(type,title,field_name,index_type,cycle,description,document_name,document_url,published) VALUES (?,?,?,?,?,?,?,?,?)", input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.documentName(), input.documentUrl(), input.published());
            return new Journal(id, input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.documentName(), input.documentUrl(), input.published());
        }
        long id = input.id();
        jdbc.update("UPDATE journals SET type=?,title=?,field_name=?,index_type=?,cycle=?,description=?,document_name=?,document_url=?,published=? WHERE id=?", input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.documentName(), input.documentUrl(), input.published(), id);
        return new Journal(id, input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.documentName(), input.documentUrl(), input.published());
    }
    public void deleteJournal(long id) { jdbc.update("DELETE FROM journals WHERE id=?", id); }

    public Submission createSubmission(Submission input) {
        if (input.authorName() == null || input.authorName().isBlank()) throw new IllegalArgumentException("请填写作者姓名");
        if (input.email() == null || input.email().isBlank()) throw new IllegalArgumentException("请填写联系邮箱");
        if (input.paperTitle() == null || input.paperTitle().isBlank()) throw new IllegalArgumentException("请填写论文标题");
        return insertSubmission(new Submission(null, input.authorName(), input.email(), input.paperTitle(), input.targetType(), input.message(), "待处理", LocalDateTime.now()));
    }

    public void attachSubmissionFile(long submissionId, String email, UploadResult upload) {
        validateSubmissionOwner(submissionId, email);
        jdbc.update(
                "INSERT INTO submission_files(submission_id,file_name,file_url,file_size) VALUES (?,?,?,?)",
                submissionId,
                upload.fileName(),
                upload.url(),
                upload.size());
    }

    public void validateSubmissionOwner(long submissionId, String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM submissions WHERE id=? AND email=?",
                Integer.class,
                submissionId,
                email);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("投稿记录不存在或联系邮箱不匹配");
        }
    }
    private Submission insertSubmission(Submission input) {
        long id = insert("INSERT INTO submissions(author_name,email,paper_title,target_type,message,status,created_at) VALUES (?,?,?,?,?,?,?)", input.authorName(), input.email(), input.paperTitle(), input.targetType(), input.message(), input.status(), Timestamp.valueOf(input.createdAt()));
        return new Submission(id, input.authorName(), input.email(), input.paperTitle(), input.targetType(), input.message(), input.status(), input.createdAt());
    }
    public List<Submission> submissions() {
        return jdbc.query("SELECT * FROM submissions ORDER BY created_at DESC", (rs, n) -> mapSubmission(rs));
    }

    public List<Submission> submissionsByEmailOrMobile(String account) {
        String pattern = "%" + account + "%";
        return jdbc.query(
                "SELECT * FROM submissions WHERE email=? OR message LIKE ? ORDER BY created_at DESC",
                (rs, n) -> mapSubmission(rs),
                account,
                pattern);
    }
    public PageResponse<Submission> submissions(String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        QueryParts query = submissionQuery(status, keyword);

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM submissions" + query.where(),
                Long.class,
                query.arguments().toArray());
        List<Object> pageArguments = new ArrayList<>(query.arguments());
        pageArguments.add(safeSize);
        pageArguments.add((safePage - 1) * safeSize);
        List<Submission> items = jdbc.query(
                "SELECT * FROM submissions" + query.where() + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapSubmission(rs),
                pageArguments.toArray());
        return PageResponse.of(items, safePage, safeSize, total == null ? 0 : total);
    }

    public List<Submission> submissionsForExport(String status, String keyword) {
        QueryParts query = submissionQuery(status, keyword);
        List<Object> arguments = new ArrayList<>(query.arguments());
        arguments.add(10000);
        return jdbc.query(
                "SELECT * FROM submissions" + query.where() + " ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> mapSubmission(rs),
                arguments.toArray());
    }

    private QueryParts submissionQuery(String status, String keyword) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> arguments = new ArrayList<>();

        if (status != null && !status.isBlank() && !"全部".equals(status)) {
            where.append(" AND status=?");
            arguments.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (author_name LIKE ? OR email LIKE ? OR paper_title LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            arguments.add(pattern);
            arguments.add(pattern);
            arguments.add(pattern);
        }
        return new QueryParts(where.toString(), arguments);
    }
    public Submission updateSubmissionStatus(long id, String status) {
        int updated = jdbc.update("UPDATE submissions SET status=? WHERE id=?", status, id);
        if (updated == 0) throw new IllegalArgumentException("投稿记录不存在");
        return submissions().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("投稿记录不存在"));
    }

    public List<ServiceOffering> publicServices() { return queryServices("SELECT * FROM service_offerings WHERE published=TRUE ORDER BY id"); }
    public List<ServiceOffering> services() { return queryServices("SELECT * FROM service_offerings ORDER BY id"); }
    private List<ServiceOffering> queryServices(String sql) {
        return jdbc.query(sql, (rs, n) -> new ServiceOffering(rs.getLong("id"), rs.getString("category"), rs.getString("title"), rs.getString("price"), rs.getString("description"), rs.getString("features"), rs.getBoolean("published")));
    }
    public ServiceOffering saveService(ServiceOffering input) {
        if (input.id() == null) {
            long id = insert("INSERT INTO service_offerings(category,title,price,description,features,published) VALUES (?,?,?,?,?,?)", input.category(), input.title(), input.price(), input.description(), input.features(), input.published());
            return new ServiceOffering(id, input.category(), input.title(), input.price(), input.description(), input.features(), input.published());
        }
        long id = input.id();
        jdbc.update("UPDATE service_offerings SET category=?,title=?,price=?,description=?,features=?,published=? WHERE id=?", input.category(), input.title(), input.price(), input.description(), input.features(), input.published(), id);
        return new ServiceOffering(id, input.category(), input.title(), input.price(), input.description(), input.features(), input.published());
    }
    public void deleteService(long id) { jdbc.update("DELETE FROM service_offerings WHERE id=?", id); }

    public List<Expert> publicExperts() { return queryExperts("SELECT * FROM experts WHERE published=TRUE ORDER BY id"); }
    public List<Expert> experts() { return queryExperts("SELECT * FROM experts ORDER BY id"); }
    private List<Expert> queryExperts(String sql) {
        return jdbc.query(sql, (rs, n) -> new Expert(rs.getLong("id"), rs.getString("name"), rs.getString("institution"), rs.getString("role_name"), rs.getString("image_url"), rs.getBoolean("published")));
    }
    public Expert saveExpert(Expert input) {
        if (input.id() == null) {
            long id = insert("INSERT INTO experts(name,institution,role_name,image_url,published) VALUES (?,?,?,?,?)", input.name(), input.institution(), input.role(), input.imageUrl(), input.published());
            return new Expert(id, input.name(), input.institution(), input.role(), input.imageUrl(), input.published());
        }
        long id = input.id();
        jdbc.update("UPDATE experts SET name=?,institution=?,role_name=?,image_url=?,published=? WHERE id=?", input.name(), input.institution(), input.role(), input.imageUrl(), input.published(), id);
        return new Expert(id, input.name(), input.institution(), input.role(), input.imageUrl(), input.published());
    }
    public void deleteExpert(long id) { jdbc.update("DELETE FROM experts WHERE id=?", id); }

    public DashboardSummary dashboardSummary() {
        List<Banner> banners = banners();
        List<Journal> journals = journals();
        List<Submission> submissions = submissions();
        return new DashboardSummary(banners.size(), banners.stream().filter(Banner::enabled).count(), journals.size(), journals.stream().filter(Journal::published).count(), submissions.size(), submissions.stream().filter(item -> "待处理".equals(item.status())).count(), submissions.stream().limit(5).toList());
    }

    private Journal mapJournal(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Journal(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("field_name"),
                rs.getString("index_type"),
                rs.getString("cycle"),
                rs.getString("description"),
                rs.getString("document_name"),
                rs.getString("document_url"),
                rs.getBoolean("published"));
    }

    private Submission mapSubmission(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Submission(
                rs.getLong("id"),
                rs.getString("author_name"),
                rs.getString("email"),
                rs.getString("paper_title"),
                rs.getString("target_type"),
                rs.getString("message"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private record QueryParts(String where, List<Object> arguments) {
    }
}
