export const staticBanners = [
  { id: 1, title: "思研学术 SCI 特刊交流中心", imageUrl: "images/optimized/hero-center-1600.webp", linkUrl: "/SCI", sortOrder: 1, enabled: true },
  { id: 2, title: "SCI 特刊快速通道", imageUrl: "images/optimized/hero-fast-track-1600.webp", linkUrl: "/SCI", sortOrder: 2, enabled: true },
];

export const staticJournals = [
  { id: 1, type: "SCI", title: "International Journal of Mental Health Studies", field: "医学与健康", indexType: "SCI / SSCI", cycle: "3-6个月", description: "心理健康、公共卫生与临床实践方向。", imageUrl: null, journalLevel: "SCI&SSCI", impactFactorLabel: "0-6", impactFactorValue: 3.2, journalPartition: "1-4区，Q1、Q2、Q3、Q4", acceptanceTime: "预计2-3个月", submissionDeadlineText: "长期征稿", submissionDeadlineDate: null, disciplineCategory: "生物学", casZone: "-", jcrQuartile: "Q2", viewCount: 0, documentName: null, documentUrl: null, published: true },
  { id: 2, type: "SCI", title: "Journal of Intelligent Systems Engineering", field: "计算机与人工智能", indexType: "SCI", cycle: "4-7个月", description: "智能计算、机器学习与复杂工程应用。", imageUrl: null, journalLevel: "SCI", impactFactorLabel: "1.5+", impactFactorValue: 1.5, journalPartition: "4区，Q3", acceptanceTime: "预计1-2个月", submissionDeadlineText: "长期征稿", submissionDeadlineDate: null, disciplineCategory: "计算机信息工程", casZone: "-", jcrQuartile: "Q3", viewCount: 0, documentName: null, documentUrl: null, published: true },
  { id: 3, type: "EI", title: "Smart Manufacturing and Industrial Systems", field: "自动化与制造", indexType: "EI Compendex", cycle: "2-4个月", description: "智能制造、工业互联网与数字孪生。", imageUrl: null, journalLevel: "EI", impactFactorLabel: "-", impactFactorValue: null, journalPartition: "-", acceptanceTime: "预计1-2个月", submissionDeadlineText: "长期征稿", submissionDeadlineDate: null, disciplineCategory: "机械电子工程", casZone: "-", jcrQuartile: "-", viewCount: 0, documentName: null, documentUrl: null, published: true },
];

export const staticServices = [
  { id: 1, category: "translation", title: "高级翻译", price: "¥0.8/字", description: "由具备学科背景的双语专家完成中英翻译，并进行术语与逻辑复核。", features: "资深译员首轮翻译\n学科领域专家审核\n15天内一次免费修订", published: true },
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
