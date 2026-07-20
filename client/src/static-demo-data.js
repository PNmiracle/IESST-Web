export const staticBanners = [
  { id: 1, title: "思研学术 - 核心期刊交流中心", imageUrl: "images/optimized/hero-academic-center-1600.jpg", linkUrl: "/about", sortOrder: 1, enabled: true },
  { id: 2, title: "核心期刊快速通道", imageUrl: "images/optimized/hero-fast-track-updated-1600.jpg", linkUrl: "/SCI", sortOrder: 2, enabled: true },
  { id: 3, title: "优秀作者扶持计划", imageUrl: "images/optimized/hero-author-support-1600.jpg", linkUrl: "/submit", sortOrder: 3, enabled: true },
];

const journalCoverCatalog = [
  [101, "SCI", "人工智能教育", "计算机信息工程", "ESCI", "1-2个月", "ai-education.jpg", "-", "-"],
  [102, "SCI", "物理 Physics", "数学物理", "SCI", "2-3个月", "physics.jpg", "-", "JCR2"],
  [103, "SCI", "人工智能·环境可持续", "环境科学工程", "SCI", "2-3个月", "ai-environment-sustainability.jpg", "-", "JCR2"],
  [104, "SCI", "工程技术", "机械电子工程", "SCI", "2-3个月", "engineering-technology.jpg", "中科院4区", "JCR3"],
  [105, "SCI", "神经医学", "医学", "SCI", "2-3个月", "neuroscience-medicine.jpg", "中科院3区", "JCR1"],
  [106, "SCI", "纳米材料", "材料科学", "SCI", "2-3个月", "nanomaterials.jpg", "中科院4区", "JCR3"],
  [107, "EI", "计算机人工智能", "计算机信息工程", "EI", "3-4个月", "computer-ai.jpg", "-", "-"],
  [108, "EI", "计算机·医学", "医学", "EI", "3-4个月", "computer-medicine.jpg", "-", "-"],
  [109, "EI", "生物医学工程", "生物学", "EI", "3-4个月", "biomedical-engineering.jpg", "-", "-"],
  [110, "EI", "计算机跨学科应用", "计算机信息工程", "EI", "3-4个月", "interdisciplinary-computing.jpg", "-", "-"],
  [111, "EI", "电子和电气", "机械电子工程", "EI", "3-4个月", "electronics-electrical.jpg", "-", "-"],
  [112, "EI", "能源与工程", "环境科学工程", "EI", "3-4个月", "energy-engineering.jpg", "-", "-"],
  [113, "EI", "电子电气工程", "机械电子工程", "EI", "3-4个月", "electronic-electrical-engineering.jpg", "-", "-"],
  [114, "EI", "多媒体图形学", "计算机信息工程", "EI", "3-4个月", "multimedia-graphics.jpg", "-", "-"],
  [115, "EI", "能源工程", "环境科学工程", "EI", "3-4个月", "energy-engineering-blue.jpg", "-", "-"],
  [116, "EI", "工程技术与燃料", "化学工程", "EI", "3-4个月", "engineering-fuels.jpg", "-", "-"],
  [117, "EI", "计算机算法", "计算机信息工程", "EI", "3-4个月", "computer-algorithms.jpg", "-", "-"],
  [118, "EI", "人文社科跨学科", "人文社科", "EI", "3-4个月", "humanities-social-sciences.jpg", "-", "-"],
  [119, "EI", "电信科学", "机械电子工程", "EI", "3-4个月", "telecommunications-science.jpg", "-", "-"],
  [120, "EI", "计算机网络", "计算机信息工程", "EI", "3-4个月", "computer-networks.jpg", "-", "-"],
  [121, "EI", "人工智能", "计算机信息工程", "EI", "3-4个月", "artificial-intelligence.jpg", "-", "-"],
  [122, "EI", "化学工程", "化学工程", "EI", "3-4个月", "chemical-engineering.jpg", "-", "-"],
].map(([id, type, title, disciplineCategory, journalLevel, acceptanceTime, image, casZone, jcrQuartile]) => ({
  id,
  type,
  title,
  field: disciplineCategory,
  indexType: journalLevel,
  cycle: acceptanceTime,
  description: `${title}方向稿件征集中，欢迎提交稿件进行适配评估。`,
  imageUrl: `images/journals/${image}`,
  journalLevel,
  impactFactorLabel: "-",
  impactFactorValue: null,
  journalPartition: [casZone, jcrQuartile].filter((value) => value !== "-").join("，") || "-",
  acceptanceTime: `预计${acceptanceTime}`,
  submissionDeadlineText: "长期征稿",
  submissionDeadlineDate: null,
  disciplineCategory,
  casZone,
  jcrQuartile,
  viewCount: 0,
  documentName: null,
  documentUrl: null,
  published: true,
}));

export const staticJournals = [
  ...journalCoverCatalog,
];

export const staticServices = [
  { id: 1, category: "translation", title: "高级翻译", price: "¥0.8/词", description: "由具备学科背景的双语专家完成中英翻译，并进行术语与逻辑复核。", features: "资深译员首轮翻译\n学科领域专家审核\n15天内一次免费修订", published: true },
  { id: 2, category: "translation", title: "深度润色", price: "¥0.5/词", description: "优化语法、术语、句式和学术表达，使全文逻辑清晰、上下文自然。", features: "专业术语编校\n语言错误全面修正\n整体结构与表达审校", published: true },
  { id: 3, category: "editing", title: "稿件诊断", price: "按稿评估", description: "从期刊适配、结构逻辑、研究亮点和发表风险等维度形成诊断建议。", features: "研究亮点评估\n结构与逻辑诊断\n目标期刊适配建议", published: true },
  { id: 4, category: "editing", title: "科学编辑", price: "定制报价", description: "围绕标题、摘要、论证结构、图表说明和研究叙事进行深度编辑。", features: "学术逻辑优化\n图表与摘要建议\n编辑修改说明", published: true },
  { id: 5, category: "editing", title: "返修支持", price: "按轮评估", description: "协助梳理审稿意见、制定回复策略，并优化逐条回复的科学表达。", features: "审稿意见分类\n回复策略建议\nResponse Letter 编辑", published: true },
];

export const staticExperts = [
  [1, "Sos S. Agaian", "City University of New York", "sos-agaian.jpg"],
  [2, "Om P. Malik", "University of Calgary", "om-malik.jpg"],
  [3, "Maike Neuhaus", "The University of Queensland", "maike-neuhaus.jpg"],
  [4, "Jumril Yunas", "Universiti Kebangsaan Malaysia", "jumril-yunas.jpg"],
  [5, "Oussama Khatib", "Stanford University", "oussama-khatib.jpg"],
  [6, "Ali M. Eltamaly", "King Saud University", "ali-eltamaly.jpg"],
].map(([id, name, institution, image]) => ({ id, name, institution, role: "学术编辑 · 编辑专家", imageUrl: `images/experts/${image}`, published: true }));

export function filterStaticJournals(parameters = {}) {
  const keyword = String(parameters.keyword || "").trim().toLowerCase();
  return staticJournals.filter((item) => {
    if (parameters.type && item.type !== parameters.type) return false;
    if (parameters.discipline && parameters.discipline !== "不限" && item.disciplineCategory !== parameters.discipline) return false;
    if (parameters.journalLevel && item.journalLevel !== parameters.journalLevel) return false;
    if (parameters.casZone && item.casZone !== parameters.casZone) return false;
    if (parameters.jcrQuartile && item.jcrQuartile !== parameters.jcrQuartile) return false;
    if (parameters.impactMin && (item.impactFactorValue ?? -Infinity) < Number(parameters.impactMin)) return false;
    if (parameters.impactMax && (item.impactFactorValue ?? Infinity) > Number(parameters.impactMax)) return false;
    return !keyword || [item.title, item.field, item.description, item.indexType, item.disciplineCategory].filter(Boolean).join(" ").toLowerCase().includes(keyword);
  });
}
