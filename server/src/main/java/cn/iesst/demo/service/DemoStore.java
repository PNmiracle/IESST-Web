package cn.iesst.demo.service;

import cn.iesst.demo.model.Banner;
import cn.iesst.demo.model.DashboardSummary;
import cn.iesst.demo.model.Expert;
import cn.iesst.demo.model.Journal;
import cn.iesst.demo.model.PageResponse;
import cn.iesst.demo.model.ServiceOffering;
import cn.iesst.demo.model.Submission;
import cn.iesst.demo.service.ManuscriptStorageService.StoredManuscript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import cn.iesst.demo.model.StoredFileInfo;

@Service
public class DemoStore {
    private final JdbcTemplate jdbc;

    public DemoStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void seedContent() {
        if (count("banners") == 0) {
            saveBanner(new Banner(null, "思研学术 SCI 特刊交流中心", "/images/optimized/hero-center-1600.webp", "/SCI", 1, true));
            saveBanner(new Banner(null, "SCI 特刊快速通道", "/images/optimized/hero-fast-track-1600.webp", "/SCI", 2, true));
        }
        if (count("journals") == 0) {
            saveJournal(new Journal(null, "SCI", "International Journal of Mental Health Studies", "医学与健康", "SCI / SSCI", "3-6个月", "心理健康、公共卫生与临床实践方向。", null, "SCI&SSCI", "0-6", 3.2, "1-4区，Q1、Q2、Q3、Q4", "预计2-3个月", "长期征稿", null, "生物学", "-", "Q2", 0L, null, null, true));
            saveJournal(new Journal(null, "SCI", "Journal of Intelligent Systems Engineering", "计算机与人工智能", "SCI", "4-7个月", "智能计算、机器学习与复杂工程应用。", null, "SCI", "1.5+", 1.5, "4区，Q3", "预计1-2个月", "长期征稿", null, "计算机信息工程", "-", "Q3", 0L, null, null, true));
            saveJournal(new Journal(null, "EI", "Smart Manufacturing and Industrial Systems", "自动化与制造", "EI Compendex", "2-4个月", "智能制造、工业互联网与数字孪生。", null, "EI", "-", null, "-", "预计1-2个月", "长期征稿", null, "机械电子工程", "-", "-", 0L, null, null, true));
            saveJournal(new Journal(null, "EI", "Electronic Information and Communication", "电子与通信", "EI Compendex", "2-4个月", "电子信息、信号处理与通信网络。", null, "EI", "-", null, "-", "预计1-2个月", "长期征稿", null, "机械电子工程", "-", "-", 0L, null, null, false));
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
        return jdbc.query("SELECT * FROM banners WHERE enabled=TRUE ORDER BY sort_order, id", (rs, n) ->
                new Banner(rs.getLong("id"), rs.getString("title"), rs.getString("image_url"), rs.getString("link_url"), rs.getInt("sort_order"), rs.getBoolean("enabled")));
    }

    public List<Banner> banners() {
        return jdbc.query("SELECT * FROM banners ORDER BY sort_order, id", (rs, n) ->
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
    public List<Journal> publicJournals(
            String type,
            String discipline,
            String deadline,
            String journalLevel,
            String casZone,
            String jcrQuartile,
            Double impactMin,
            Double impactMax,
            String keyword,
            String sort) {
        Comparator<Journal> comparator = journalComparator(sort);
        return publicJournals().stream()
                .filter(item -> matchesOption(item.type(), type))
                .filter(item -> matchesOption(item.disciplineCategory(), discipline))
                .filter(item -> matchesOption(item.journalLevel(), journalLevel))
                .filter(item -> matchesOption(item.casZone(), casZone))
                .filter(item -> matchesOption(item.jcrQuartile(), jcrQuartile))
                .filter(item -> impactWithin(item.impactFactorValue(), impactMin, impactMax))
                .filter(item -> deadlineWithin(item.submissionDeadlineDate(), deadline))
                .filter(item -> matchesKeyword(item, keyword))
                .sorted(comparator)
                .toList();
    }
    public List<Journal> journals() { return queryJournals("SELECT * FROM journals ORDER BY id"); }
    public Journal publicJournal(long id) {
        jdbc.update("UPDATE journals SET view_count=view_count+1 WHERE id=? AND published=TRUE", id);
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
            long id = insert("INSERT INTO journals(type,title,field_name,index_type,cycle,description,image_url,journal_level,impact_factor_label,impact_factor_value,journal_partition,acceptance_time,submission_deadline_text,submission_deadline_date,discipline_category,cas_zone,jcr_quartile,view_count,document_name,document_url,published) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.imageUrl(), input.journalLevel(), input.impactFactorLabel(), input.impactFactorValue(), input.journalPartition(), input.acceptanceTime(), input.submissionDeadlineText(), blankToNull(input.submissionDeadlineDate()), input.disciplineCategory(), input.casZone(), input.jcrQuartile(), safeViewCount(input.viewCount()), input.documentName(), input.documentUrl(), input.published());
            return withJournalId(id, input);
        }
        long id = input.id();
        jdbc.update("UPDATE journals SET type=?,title=?,field_name=?,index_type=?,cycle=?,description=?,image_url=?,journal_level=?,impact_factor_label=?,impact_factor_value=?,journal_partition=?,acceptance_time=?,submission_deadline_text=?,submission_deadline_date=?,discipline_category=?,cas_zone=?,jcr_quartile=?,view_count=?,document_name=?,document_url=?,published=? WHERE id=?", input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.imageUrl(), input.journalLevel(), input.impactFactorLabel(), input.impactFactorValue(), input.journalPartition(), input.acceptanceTime(), input.submissionDeadlineText(), blankToNull(input.submissionDeadlineDate()), input.disciplineCategory(), input.casZone(), input.jcrQuartile(), safeViewCount(input.viewCount()), input.documentName(), input.documentUrl(), input.published(), id);
        return withJournalId(id, input);
    }
    public void deleteJournal(long id) { jdbc.update("DELETE FROM journals WHERE id=?", id); }

    public Submission createSubmission(Submission input) {
        if (input.authorName() == null || input.authorName().isBlank()) throw new IllegalArgumentException("请填写作者姓名");
        if (input.email() == null || input.email().isBlank()) throw new IllegalArgumentException("请填写联系邮箱");
        if (input.paperTitle() == null || input.paperTitle().isBlank()) throw new IllegalArgumentException("请填写论文标题");
        return insertSubmission(new Submission(null, input.authorName(), input.email(), input.paperTitle(), input.targetType(), input.message(), "待处理", LocalDateTime.now()));
    }

    public void attachSubmissionFile(long submissionId, StoredManuscript upload) {
        jdbc.update(
                "INSERT INTO submission_files(submission_id,file_name,storage_key,file_size,content_type) VALUES (?,?,?,?,?)",
                submissionId,
                upload.fileName(),
                upload.storageKey(),
                upload.size(),
                upload.contentType());
    }

    public List<StoredFileInfo> submissionFiles(long submissionId) {
        return jdbc.query(
                "SELECT id,file_name,file_size,content_type,uploaded_at FROM submission_files WHERE submission_id=? ORDER BY uploaded_at DESC",
                (rs, rowNum) -> new StoredFileInfo(
                        rs.getLong("id"),
                        rs.getString("file_name"),
                        rs.getLong("file_size"),
                        rs.getString("content_type"),
                        rs.getTimestamp("uploaded_at").toLocalDateTime()),
                submissionId);
    }

    public StoredFileRecord submissionFile(long submissionId, long fileId) {
        return jdbc.query(
                        "SELECT id,file_name,storage_key,file_size,content_type,uploaded_at FROM submission_files WHERE submission_id=? AND id=?",
                        (rs, rowNum) -> new StoredFileRecord(
                                rs.getLong("id"),
                                rs.getString("file_name"),
                                rs.getString("storage_key"),
                                rs.getLong("file_size"),
                                rs.getString("content_type"),
                                rs.getTimestamp("uploaded_at").toLocalDateTime()),
                        submissionId,
                        fileId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("稿件附件不存在"));
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
        var deadline = rs.getDate("submission_deadline_date");
        return new Journal(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("field_name"),
                rs.getString("index_type"),
                rs.getString("cycle"),
                rs.getString("description"),
                rs.getString("image_url"),
                rs.getString("journal_level"),
                rs.getString("impact_factor_label"),
                nullableDouble(rs, "impact_factor_value"),
                rs.getString("journal_partition"),
                rs.getString("acceptance_time"),
                rs.getString("submission_deadline_text"),
                deadline == null ? null : deadline.toLocalDate().toString(),
                rs.getString("discipline_category"),
                rs.getString("cas_zone"),
                rs.getString("jcr_quartile"),
                rs.getLong("view_count"),
                rs.getString("document_name"),
                rs.getString("document_url"),
                rs.getBoolean("published"));
    }

    private Journal withJournalId(long id, Journal input) {
        return new Journal(id, input.type(), input.title(), input.field(), input.indexType(), input.cycle(), input.description(), input.imageUrl(), input.journalLevel(), input.impactFactorLabel(), input.impactFactorValue(), input.journalPartition(), input.acceptanceTime(), input.submissionDeadlineText(), input.submissionDeadlineDate(), input.disciplineCategory(), input.casZone(), input.jcrQuartile(), safeViewCount(input.viewCount()), input.documentName(), input.documentUrl(), input.published());
    }

    private long safeViewCount(Long viewCount) {
        return viewCount == null ? 0L : viewCount;
    }

    private Double nullableDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private boolean matchesOption(String actual, String expected) {
        if (expected == null || expected.isBlank() || "ALL".equalsIgnoreCase(expected) || "全部".equals(expected) || "不限".equals(expected)) return true;
        return expected.equalsIgnoreCase(actual == null ? "" : actual);
    }

    private boolean impactWithin(Double value, Double min, Double max) {
        if (value == null) return min == null && max == null;
        return (min == null || value >= min) && (max == null || value <= max);
    }

    private boolean deadlineWithin(String dateText, String range) {
        if (range == null || range.isBlank() || "不限".equals(range) || "ALL".equalsIgnoreCase(range)) return true;
        LocalDate deadline = parseDate(dateText);
        if (deadline == null) return false;
        LocalDate today = LocalDate.now();
        int days = switch (range) {
            case "week" -> 7;
            case "month" -> 30;
            case "quarter" -> 90;
            case "halfYear" -> 183;
            case "year" -> 365;
            default -> 0;
        };
        if (days == 0) return true;
        return !deadline.isBefore(today) && !deadline.isAfter(today.plusDays(days));
    }

    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.isBlank()) return null;
        try {
            return LocalDate.parse(dateText);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private boolean matchesKeyword(Journal item, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String haystack = String.join(" ",
                nullToEmpty(item.title()),
                nullToEmpty(item.field()),
                nullToEmpty(item.description()),
                nullToEmpty(item.journalLevel()),
                nullToEmpty(item.disciplineCategory()));
        return haystack.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Comparator<Journal> journalComparator(String sort) {
        if ("deadline".equals(sort)) {
            return Comparator.comparing((Journal item) -> parseDate(item.submissionDeadlineDate()), Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("impact".equals(sort)) {
            return Comparator.comparing((Journal item) -> item.impactFactorValue(), Comparator.nullsLast(Comparator.reverseOrder()));
        }
        if ("click".equals(sort)) {
            return Comparator.comparing((Journal item) -> item.viewCount() == null ? 0L : item.viewCount(), Comparator.reverseOrder());
        }
        return Comparator.comparing(Journal::id);
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
