<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { api } from "../../api";
import JournalCard from "../../components/JournalCard.vue";

const defaultBanners = [
  { id: "default-center", title: "思研学术 SCI 特刊交流中心", imageUrl: "/images/optimized/hero-center-1600.webp", linkUrl: "/SCI", sortOrder: 1, enabled: true },
  { id: "default-fast-track", title: "SCI 特刊快速通道", imageUrl: "/images/optimized/hero-fast-track-1600.webp", linkUrl: "/SCI", sortOrder: 2, enabled: true },
];
const banners = ref(defaultBanners);
const journals = ref([]);
const experts = ref([]);
const currentSlide = ref(0);
const loading = ref(true);
const loadError = ref("");
const paused = ref(false);
const journalType = ref("ALL");
const keyword = ref("");
const journalPage = ref(0);
const editorQrOpen = ref(false);
const expertSlide = ref(0);
const activeFaqIndex = ref(0);
const resourceQrOpen = ref(false);
const selectedResource = ref(null);
const submissionSteps = [
  { title: "建立个人投稿档案", text: "稿件信息会进入管理员投稿列表，便于编辑跟进。" },
  { title: "编辑初步评估", text: "结合研究方向、目标类型和稿件阶段判断服务范围。" },
  { title: "同步进度反馈", text: "登录学生账号提交时，可在我的订单中查看处理状态。" },
];
const mobileShortcutItems = [
  { label: "首页", icon: "home", to: "/" },
  { label: "SCI期刊", icon: "book", to: "/SCI" },
  { label: "EI期刊", icon: "journal", to: "/EI" },
  { label: "翻译润色", icon: "translate", to: "/services/translation" },
  { label: "科学编辑", icon: "edit", to: "/services/editing" },
  { label: "提交评估", icon: "submit", to: "/submit" },
];
const faqs = [
  {
    question: "录用、出版、检索周期是什么样的？",
    answer: "录用一般在 2–6 个月，部分刊物录用即出版，部分刊物在录用后 1–3 个月出版；出版后通常 2–3 个月可完成检索。",
  },
  {
    question: "论文被检索后是否开具检索证明？",
    answer: "所有已检索的论文，我们都会免费提供检索证明。",
  },
  {
    question: "如何申请开票？",
    answer: "请联系客户服务并提供订单号申请开票。其中，版面费部分可提供期刊部官方 Invoice。",
  },
  {
    question: "为什么不能告知期刊名称？",
    answer: "为减少期刊名称在市场上的流通与大规模传播，保护期刊长期稳定检索，付款前仅提供期刊分区、影响因子、征稿方向等相关信息，不提供期刊名称。",
  },
  {
    question: "如果我是人文社科类的作者，我可以投稿 EI 或者 SCI 吗？",
    answer: "可以。具体的研究方向匹配与投稿方案，请咨询编辑老师。",
  },
];
const academicResources = [
  {
    title: "最新 SCI 目录",
    subtitle: "SCI JOURNAL LIST",
    description: ["快速了解当前 SCI ", { text: "期刊资源", keep: true }, "与", { text: "研究方向", keep: true }, "。"],
    badge: "SCI",
  },
  {
    title: "最新 EI 目录",
    subtitle: "EI JOURNAL LIST",
    description: ["查阅 EI ", { text: "收录期刊", keep: true }, "与学科", { text: "覆盖情况", keep: true }, "。"],
    badge: "EI",
  },
  {
    title: "中科院期刊分区表（2025年）",
    subtitle: "CAS JOURNAL RANKING",
    description: ["获取", { text: "期刊分区", keep: true }, "参考，辅助", { text: "目标刊物", keep: true }, "筛选。"],
    badge: "2025",
  },
  {
    title: "新锐期刊分区表（最新版）",
    subtitle: "EMERGING JOURNALS",
    description: ["探索新锐", { text: "期刊", keep: true }, "的分区与", { text: "研究方向", keep: true }, "。"],
    badge: "NEW",
  },
];
const advantages = [
  {
    title: "权威期刊资源",
    text: "思研学术专注于 SCI 特刊，涵盖各个学科领域，提供多元化发表通道",
    icon: "book",
  },
  {
    title: "专业编辑团队",
    text: "由 150 余位专业教授编委团队提前审稿，审核稿件内容，提高论文质量，确保投准、投中",
    icon: "team",
  },
  {
    title: "高效发表流程",
    text: "简化发表流程，缩短审核周期，提供全程进度跟踪，确保论文快速高效发表",
    icon: "flow",
  },
  {
    title: "高被引保障",
    text: "提供论文推广服务，增加论文曝光度，提高论文被引用率，提升学术影响力",
    icon: "quote",
  },
  {
    title: "编辑1对1咨询",
    text: "直接和期刊编辑老师对话，提供专业的学术咨询服务，根据需求定制发表方案",
    icon: "chat",
  },
  {
    title: "安全保障",
    text: "严格保护客户隐私和论文版权，确保论文内容不被泄露，提供安全可靠的服务保障",
    icon: "shield",
  },
];
let carouselTimer;
const optimizedImageMap = {
  "/images/hero-center.jpg": "/images/optimized/hero-center-1600.webp",
  "/images/hero-fast-track.jpg": "/images/optimized/hero-fast-track-1600.webp",
  "/images/optimized/hero-center-1200.webp": "/images/optimized/hero-center-1600.webp",
  "/images/optimized/hero-center-2000.webp": "/images/optimized/hero-center-1600.webp",
  "/images/optimized/hero-fast-track-1200.webp": "/images/optimized/hero-fast-track-1600.webp",
  "/images/optimized/hero-fast-track-2000.webp": "/images/optimized/hero-fast-track-1600.webp",
};
const heroImageSets = {
  "/images/hero-center.jpg": "/images/optimized/hero-center-800.webp 800w, /images/optimized/hero-center-1200.webp 1200w, /images/optimized/hero-center-1600.webp 1600w, /images/optimized/hero-center-2400.webp 2400w, /images/optimized/hero-center-3200.webp 3200w",
  "/images/optimized/hero-center-1200.webp": "/images/optimized/hero-center-800.webp 800w, /images/optimized/hero-center-1200.webp 1200w, /images/optimized/hero-center-1600.webp 1600w, /images/optimized/hero-center-2400.webp 2400w, /images/optimized/hero-center-3200.webp 3200w",
  "/images/optimized/hero-center-1600.webp": "/images/optimized/hero-center-800.webp 800w, /images/optimized/hero-center-1200.webp 1200w, /images/optimized/hero-center-1600.webp 1600w, /images/optimized/hero-center-2400.webp 2400w, /images/optimized/hero-center-3200.webp 3200w",
  "/images/optimized/hero-center-2000.webp": "/images/optimized/hero-center-800.webp 800w, /images/optimized/hero-center-1200.webp 1200w, /images/optimized/hero-center-1600.webp 1600w, /images/optimized/hero-center-2400.webp 2400w, /images/optimized/hero-center-3200.webp 3200w",
  "/images/hero-fast-track.jpg": "/images/optimized/hero-fast-track-800.webp 800w, /images/optimized/hero-fast-track-1200.webp 1200w, /images/optimized/hero-fast-track-1600.webp 1600w, /images/optimized/hero-fast-track-2400.webp 2400w, /images/optimized/hero-fast-track-3200.webp 3200w",
  "/images/optimized/hero-fast-track-1200.webp": "/images/optimized/hero-fast-track-800.webp 800w, /images/optimized/hero-fast-track-1200.webp 1200w, /images/optimized/hero-fast-track-1600.webp 1600w, /images/optimized/hero-fast-track-2400.webp 2400w, /images/optimized/hero-fast-track-3200.webp 3200w",
  "/images/optimized/hero-fast-track-1600.webp": "/images/optimized/hero-fast-track-800.webp 800w, /images/optimized/hero-fast-track-1200.webp 1200w, /images/optimized/hero-fast-track-1600.webp 1600w, /images/optimized/hero-fast-track-2400.webp 2400w, /images/optimized/hero-fast-track-3200.webp 3200w",
  "/images/optimized/hero-fast-track-2000.webp": "/images/optimized/hero-fast-track-800.webp 800w, /images/optimized/hero-fast-track-1200.webp 1200w, /images/optimized/hero-fast-track-1600.webp 1600w, /images/optimized/hero-fast-track-2400.webp 2400w, /images/optimized/hero-fast-track-3200.webp 3200w",
};
const heroImageSizes = "100vw";
const preloadedImages = new Set();

const activeBanner = computed(() => banners.value[currentSlide.value] || null);
const filteredJournals = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  return journals.value.filter((item) => {
    const matchesType = journalType.value === "ALL" || item.type === journalType.value;
    const searchable = [
      item.title,
      item.field,
      item.description,
      item.indexType,
      item.journalLevel,
      item.disciplineCategory,
      item.journalPartition,
    ].filter(Boolean).join(" ");
    const matchesKeyword = !query || searchable.toLowerCase().includes(query);
    return matchesType && matchesKeyword;
  });
});
const journalsPerPage = 4;
const journalPageCount = computed(() => Math.max(1, Math.ceil(filteredJournals.value.length / journalsPerPage)));
const visibleJournals = computed(() => {
  const start = journalPage.value * journalsPerPage;
  return filteredJournals.value.slice(start, start + journalsPerPage);
});
const expertMatchRates = ["96.2%", "97.9%", "95.8%", "98.1%", "96.7%", "97.4%"];
const visibleExperts = computed(() => {
  const total = experts.value.length;
  if (!total) return [];
  return [-1, 0, 1].map((offset) => {
    const index = (expertSlide.value + offset + total) % total;
    return {
      ...experts.value[index],
      position: offset,
      matchRate: expertMatchRates[index % expertMatchRates.length],
    };
  });
});

function optimizedBannerImage(url) {
  return optimizedImageMap[url] || url;
}

function bannerSrcset(url) {
  return heroImageSets[url] || "";
}

function preloadImage(url) {
  if (!url || preloadedImages.has(url)) return;
  preloadedImages.add(url);
  const link = document.createElement("link");
  link.rel = "preload";
  link.as = "image";
  link.href = url;
  const srcset = bannerSrcset(url);
  if (srcset) {
    link.imageSrcset = srcset;
    link.imageSizes = heroImageSizes;
  }
  document.head.appendChild(link);
}

function showSlide(index) {
  if (!banners.value.length) return;
  currentSlide.value = (index + banners.value.length) % banners.value.length;
}

function showExpert(step) {
  if (!experts.value.length) return;
  expertSlide.value = (expertSlide.value + step + experts.value.length) % experts.value.length;
}

function toggleFaq(index) {
  activeFaqIndex.value = activeFaqIndex.value === index ? -1 : index;
}

function openResource(resource) {
  selectedResource.value = resource;
  resourceQrOpen.value = true;
}

function closeResourceQr() {
  resourceQrOpen.value = false;
}

function resourceTitleParts(title = "") {
  const bracketIndex = title.indexOf("（");
  if (bracketIndex < 0) return [{ text: title, keep: false, breakBefore: false }];
  return [
    { text: title.slice(0, bracketIndex), keep: false, breakBefore: false },
    { text: title.slice(bracketIndex), keep: true, breakBefore: true },
  ];
}

function showJournalPage(index) {
  journalPage.value = (index + journalPageCount.value) % journalPageCount.value;
}

function startCarousel() {
  clearInterval(carouselTimer);
  carouselTimer = setInterval(() => {
    if (!paused.value) showSlide(currentSlide.value + 1);
  }, 5000);
}

async function loadData() {
  loading.value = true;
  loadError.value = "";
  try {
    const [bannerItems, journalItems, expertItems] = await Promise.all([api.publicBanners(), api.publicJournals(), api.publicExperts()]);
    banners.value = bannerItems.length ? bannerItems : defaultBanners;
    if (currentSlide.value >= banners.value.length) currentSlide.value = 0;
    journals.value = journalItems;
    experts.value = expertItems;
    startCarousel();
  } catch (error) {
    loadError.value = "官网数据加载失败，请确认后端服务已启动。";
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
onBeforeUnmount(() => clearInterval(carouselTimer));
watch(activeBanner, (banner) => preloadImage(optimizedBannerImage(banner?.imageUrl)), { immediate: true });
watch([journalType, keyword], () => {
  journalPage.value = 0;
});
watch(journalPageCount, (count) => {
  if (journalPage.value >= count) journalPage.value = Math.max(0, count - 1);
});
</script>

<template>
  <div class="home-page">
    <h1 class="visually-hidden">思研学术 SCI 特刊交流中心</h1>
    <section class="site-hero">
      <div class="shell">
        <div v-if="activeBanner" class="hero-image hero-carousel" @mouseenter="paused = true" @mouseleave="paused = false">
          <button class="hero-arrow hero-arrow-left" aria-label="上一张" @click="showSlide(currentSlide - 1)">‹</button>
          <RouterLink class="hero-main-slide" to="/submit?subject=首页首屏&target=SCI">
            <img :src="optimizedBannerImage(activeBanner.imageUrl)" :srcset="bannerSrcset(activeBanner.imageUrl)" :sizes="heroImageSizes" :alt="activeBanner.title" loading="eager" fetchpriority="high" decoding="async" />
            <span class="hero-copy" aria-hidden="true">
              <b>{{ activeBanner.title }}</b>
              <small>提交稿件后，编辑将协助完成方向评估、期刊匹配与服务建议。</small>
              <em>了解更多</em>
            </span>
          </RouterLink>
          <button class="hero-arrow hero-arrow-right" aria-label="下一张" @click="showSlide(currentSlide + 1)">›</button>
          <div v-if="banners.length > 1" class="hero-dots" aria-label="轮播图切换状态">
            <button
              v-for="(banner, index) in banners"
              :key="banner.id || `${banner.imageUrl}-${index}`"
              type="button"
              class="hero-dot"
              :class="{ 'is-active': index === currentSlide }"
              :aria-label="`切换到第 ${index + 1} 张：${banner.title}`"
              :aria-current="index === currentSlide ? 'true' : undefined"
              @click="showSlide(index)"
            ></button>
          </div>
        </div>
        <div v-else-if="loading" class="hero-skeleton">轮播内容加载中…</div>
        <div v-else class="hero-skeleton error-state">{{ loadError }}<button class="ghost" @click="loadData">重新加载</button></div>
      </div>
    </section>

    <section class="mobile-shortcut-section" aria-label="移动端快捷入口">
      <div class="mobile-shortcut-grid">
        <RouterLink v-for="item in mobileShortcutItems" :key="item.label" :to="item.to" class="mobile-shortcut-item">
          <span :class="['mobile-shortcut-icon', `icon-${item.icon}`]" aria-hidden="true"></span>
          <b>{{ item.label }}</b>
        </RouterLink>
      </div>
    </section>

    <section class="section advantage-section">
      <div class="shell">
        <div class="advantage-heading"><span></span><div><h2>学术服务发表优势</h2><p>思研学术，让 SCI 期刊发表更快速更便捷</p></div><span></span></div>
        <div class="advantage-grid">
          <article v-for="item in advantages" :key="item.title" class="advantage-card">
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.text }}</p>
            </div>
            <img class="advantage-icon" :src="`/images/advantage-icons/${item.icon}.png`" alt="" aria-hidden="true" loading="lazy" decoding="async" />
          </article>
        </div>
        <div class="advantage-actions">
          <RouterLink class="primary" to="/submit?subject=学术服务发表优势&target=SCI">提交稿件评估</RouterLink>
        </div>
      </div>
    </section>

    <section class="section pale">
      <div class="shell solution-grid"><figure class="solution-cover"><img src="/images/optimized/sci-journals-books-cutout-720.webp" srcset="/images/optimized/sci-journals-books-cutout-520.webp 520w, /images/optimized/sci-journals-books-cutout-720.webp 720w, /images/optimized/sci-journals-books-cutout-900.webp 900w" sizes="(max-width: 650px) 88vw, 42vw" alt="SCI 期刊封面组合" loading="eager" fetchpriority="low" decoding="async" /></figure><div class="solution-copy"><h2 class="solution-title">可靠的SCI全流程解决方案</h2><p>论文写完了，发表才刚刚开始。选刊拿不准、流程摸不透、语言不过关、审稿意见不知如何回复——每一个环节都可能拖上数月。思研学术 SCI 特刊快速通道，将上述问题一并纳入标准服务流程：精准匹配已授权的正规特刊、编委团队前置审稿、专业编辑语言润色、审稿意见协同回应。</p><p>我们不替您写论文，不替您伪造审稿，只做一件事——帮您把合格的稿件，高效、合规地送进特刊的录用通道。</p><p>您负责把研究做扎实，我们把发表做简单。</p><div class="solution-actions"><RouterLink class="primary" to="/SCI">了解更多</RouterLink></div></div></div>
    </section>

    <section class="section shell">
      <div class="heading"><div><span>ACADEMIC SERVICES</span><h2>论文服务</h2></div><p>根据稿件所处阶段选择语言或科学编辑服务。</p></div>
      <div class="service-entry-grid">
        <RouterLink class="service-entry translation" to="/services/translation"><span>LANGUAGE SERVICES</span><h3>翻译润色</h3><p>高级翻译、母语润色与深度语言编辑。</p><b>查看服务 →</b></RouterLink>
        <article class="service-entry editing">
          <span>SCIENTIFIC EDITING</span>
          <h3>科学编辑</h3>
          <p>稿件诊断、科学编辑与返修支持。</p>
          <div class="service-entry-actions">
            <RouterLink to="/submit?mode=manuscript&target=SCI&subject=首页论文提交">论文提交</RouterLink>
            <button type="button" aria-haspopup="dialog" @click="editorQrOpen = true">咨询编辑</button>
          </div>
        </article>
      </div>
    </section>

    <section class="quick-service">
      <div class="shell service-points">
        <article><b>01</b><div><strong>正规期刊资源</strong><span>SCI / EI 多学科方向</span></div></article>
        <article><b>02</b><div><strong>稿件专业评估</strong><span>匹配方向与发表周期</span></div></article>
        <article><b>03</b><div><strong>编辑协同支持</strong><span>语言、结构与回复优化</span></div></article>
        <article><b>04</b><div><strong>流程清晰透明</strong><span>全程跟进关键节点</span></div></article>
      </div>
    </section>

    <section class="section knowledge-hub-section">
      <div class="shell knowledge-hub-grid">
        <div class="faq-panel">
          <div class="knowledge-heading">
            <span class="eyebrow">HELP CENTER</span>
            <h2>常见问题</h2>
            <p>关于投稿、出版、检索与服务流程的说明。</p>
          </div>
          <div class="faq-list">
            <article v-for="(faq, index) in faqs" :key="faq.question" class="faq-item" :class="{ 'is-open': activeFaqIndex === index }">
              <button type="button" class="faq-trigger" :aria-expanded="activeFaqIndex === index" @click="toggleFaq(index)">
                <span class="faq-number">{{ String(index + 1).padStart(2, "0") }}</span>
                <strong>{{ faq.question }}</strong>
                <span class="faq-toggle" aria-hidden="true"></span>
              </button>
              <div v-show="activeFaqIndex === index" class="faq-answer"><p>{{ faq.answer }}</p></div>
            </article>
          </div>
        </div>

        <div class="resource-panel">
          <div class="knowledge-heading">
            <span class="eyebrow">ACADEMIC RESOURCE</span>
            <h2>学术资源下载</h2>
            <p>选择所需资源，扫码添加编辑微信后即可获取。</p>
          </div>
          <div class="resource-grid">
            <button v-for="resource in academicResources" :key="resource.title" type="button" class="resource-card" @click="openResource(resource)">
              <span class="resource-card-top"><small>{{ resource.subtitle }}</small><b>{{ resource.badge }}</b></span>
              <strong>
                <template v-for="(part, partIndex) in resourceTitleParts(resource.title)" :key="partIndex">
                  <br v-if="part.breakBefore" />
                  <span :class="{ 'resource-title-suffix': part.keep }">{{ part.text }}</span>
                </template>
              </strong>
              <span class="resource-description">
                <template v-for="(part, partIndex) in resource.description" :key="partIndex">
                  <span v-if="typeof part === 'object'" :class="{ 'keep-together': part.keep }">{{ part.text }}</span>
                  <template v-else>{{ part }}</template>
                </template>
              </span>
              <em>扫码获取 <i aria-hidden="true">↗</i></em>
            </button>
          </div>
        </div>
      </div>
    </section>

    <section id="journals" class="section shell">
      <div class="heading"><div><span>JOURNAL CATALOG</span><h2>精选期刊方向</h2></div><p>选择类型或输入关键词，快速查找适合的研究方向。</p></div>
      <div class="journal-tools">
        <div class="segmented"><button v-for="type in ['ALL', 'SCI', 'EI']" :key="type" :class="{ active: journalType === type }" @click="journalType = type">{{ type === "ALL" ? "全部期刊" : type + " 期刊" }}</button></div>
        <input v-model="keyword" type="search" placeholder="搜索期刊名称、学科方向或关键词" />
      </div>
      <div v-if="loading" class="empty-state">期刊列表加载中…</div>
      <div v-else-if="filteredJournals.length" class="home-journal-carousel" aria-label="精选期刊轮播">
        <div class="journal-showcase-grid home-journal-grid">
          <JournalCard v-for="journal in visibleJournals" :key="journal.id" :journal="journal" />
        </div>
        <div v-if="journalPageCount > 1" class="home-journal-controls">
          <button type="button" aria-label="上一组期刊" @click="showJournalPage(journalPage - 1)">‹</button>
          <div class="home-journal-dots" aria-label="期刊轮播页码">
            <button
              v-for="page in journalPageCount"
              :key="page"
              type="button"
              :class="{ active: journalPage === page - 1 }"
              :aria-label="`切换到第 ${page} 组期刊`"
              :aria-current="journalPage === page - 1 ? 'page' : undefined"
              @click="showJournalPage(page - 1)"
            ></button>
          </div>
          <button type="button" aria-label="下一组期刊" @click="showJournalPage(journalPage + 1)">›</button>
        </div>
      </div>
      <div v-else class="empty-state"><b>没有匹配的期刊</b><span>调整筛选条件或提交稿件信息，由编辑协助选刊。</span></div>
    </section>

    <section id="submission" class="section pale home-assessment-band">
      <div class="shell">
        <div><span class="eyebrow">ONE CLEAR PATH</span><h2>从稿件评估开始</h2><p>上传稿件或填写需求，编辑确认服务方案；登录学生账号后，可在订单中心持续查看进度与文件。</p></div>
        <div class="home-assessment-flow"><article v-for="(step, index) in submissionSteps" :key="step.title"><b>{{ String(index + 1).padStart(2, "0") }}</b><span>{{ step.title }}</span></article></div>
        <div class="home-assessment-action">
          <img src="/images/editor-contact-qr.png" alt="编辑咨询二维码" loading="lazy" decoding="async" />
          <small>扫码咨询编辑</small>
          <RouterLink class="primary" to="/submit?subject=首页统一投稿入口&target=SCI">提交稿件评估</RouterLink>
        </div>
      </div>
    </section>

    <section class="section expert-showcase-section">
      <div class="shell">
        <div class="heading"><div><span>GLOBAL EXPERTS</span><h2>专家团队，汇聚全球学者</h2></div><RouterLink to="/about">了解团队与机构 →</RouterLink></div>
        <div class="expert-match-summary">
          <h3><strong>3000+</strong> 小领域专家匹配，<strong>1300+</strong> 细分学科领域覆盖</h3>
          <p>按研究方向与稿件阶段匹配学科专家，为选刊评估、科学编辑和投稿过程提供专业支持。</p>
        </div>
        <div v-if="visibleExperts.length" class="expert-carousel" aria-label="专家团队轮播">
          <article
            v-for="expert in visibleExperts"
            :key="`${expert.id}-${expert.position}`"
            :class="['expert-carousel-card', { active: expert.position === 0, side: expert.position !== 0 }]"
            :aria-hidden="expert.position !== 0"
          >
            <img :src="expert.imageUrl" :alt="expert.position === 0 ? expert.name : ''" loading="lazy" decoding="async" />
            <div class="expert-profile-main">
              <div class="expert-profile-title"><div><h3>{{ expert.name }}</h3><span aria-label="五星专家">★★★★★</span></div><strong>{{ expert.matchRate }}<small>客户好评率</small></strong></div>
              <em>SCI 特刊学术编辑</em>
              <dl><div><dt>大学学科：</dt><dd>{{ expert.institution }}</dd></div><div><dt>擅长领域：</dt><dd>稿件评估、科学编辑与投稿建议</dd></div><div><dt>学术背景：</dt><dd>{{ expert.role }}</dd></div></dl>
            </div>
          </article>
          <button class="expert-carousel-control previous" type="button" aria-label="上一位专家" @click="showExpert(-1)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14.5 6-6 6 6 6" /></svg>
          </button>
          <button class="expert-carousel-control next" type="button" aria-label="下一位专家" @click="showExpert(1)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9.5 6 6 6-6 6" /></svg>
          </button>
        </div>
        <RouterLink class="expert-more-link" to="/about">查看更多编辑</RouterLink>
      </div>
    </section>

    <section class="section pale"><div class="shell partner-block"><div><span class="eyebrow">PUBLISHER NETWORK</span><h2>国际期刊和出版社合作资源</h2><p>正式版本可将合作单位拆分为后台可维护的独立数据项，并增加资质与合作说明。</p><RouterLink class="ghost" to="/about">了解机构介绍</RouterLink></div><img src="/images/optimized/publisher-partners-1200.webp" alt="国际期刊和出版社合作资源" loading="lazy" decoding="async" /></div></section>

    <Teleport to="body">
      <div v-if="editorQrOpen" class="service-qr-backdrop" @click.self="editorQrOpen = false">
        <section class="service-qr-modal card" role="dialog" aria-modal="true" aria-labelledby="editor-qr-title">
          <button class="consult-close" type="button" aria-label="关闭二维码" @click="editorQrOpen = false">×</button>
          <span class="eyebrow">CONTACT EDITOR</span>
          <h2 id="editor-qr-title">咨询编辑</h2>
          <p>请使用微信扫描二维码，添加编辑进行一对一咨询。</p>
          <img src="/images/editor-contact-qr.png" alt="编辑咨询二维码" />
          <small>扫码后请备注您的研究方向与稿件阶段</small>
        </section>
      </div>
      <div v-if="resourceQrOpen" class="service-qr-backdrop" @click.self="closeResourceQr">
        <section class="service-qr-modal resource-qr-modal card" role="dialog" aria-modal="true" aria-labelledby="resource-qr-title">
          <button class="consult-close" type="button" aria-label="关闭二维码" @click="closeResourceQr">×</button>
          <span class="eyebrow">WECHAT RESOURCE ACCESS</span>
          <h2 id="resource-qr-title">
            <template v-for="(part, partIndex) in resourceTitleParts(selectedResource?.title)" :key="partIndex">
              <br v-if="part.breakBefore" />
              <span :class="{ 'modal-keep': part.keep }">{{ part.text }}</span>
            </template>
          </h2>
          <p>请使用微信扫描二维码，添加编辑后备注<span class="modal-keep">“{{ selectedResource?.title }}”</span>，<br />即可获取对应资源。</p>
          <img src="/images/editor-contact-qr.png" alt="添加编辑微信二维码" />
          <small>资源由编辑核验后发送，请勿重复扫码</small>
        </section>
      </div>
    </Teleport>
  </div>
</template>
