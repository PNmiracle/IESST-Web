<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { api } from "../../api";

const banners = ref([]);
const journals = ref([]);
const experts = ref([]);
const currentSlide = ref(0);
const loading = ref(true);
const loadError = ref("");
const paused = ref(false);
const journalType = ref("ALL");
const keyword = ref("");
const submissionSteps = [
  { title: "后台生成记录", text: "稿件信息会进入管理员投稿列表，便于顾问跟进。" },
  { title: "顾问初步评估", text: "结合研究方向、目标类型和稿件阶段判断服务范围。" },
  { title: "同步进度反馈", text: "登录学生账号提交时，可在我的订单中查看处理状态。" },
];
const advantages = [
  {
    title: "权威期刊资源",
    text: "思研学术专注于 SCI 特刊，涵盖各个学科领域，提供多元化发表通道",
    icon: "book",
  },
  {
    title: "专业编辑团队",
    text: "由 150 余位教授编委团队提前审稿，审核稿件内容，提高论文质量，确保投准、投中",
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

const activeBanner = computed(() => banners.value[currentSlide.value] || null);
const previousBanner = computed(() => {
  if (banners.value.length < 2) return null;
  return banners.value[(currentSlide.value - 1 + banners.value.length) % banners.value.length];
});
const nextBanner = computed(() => {
  if (banners.value.length < 2) return null;
  return banners.value[(currentSlide.value + 1) % banners.value.length];
});
const filteredJournals = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  return journals.value.filter((item) => {
    const matchesType = journalType.value === "ALL" || item.type === journalType.value;
    const matchesKeyword = !query || `${item.title} ${item.field} ${item.description} ${item.indexType}`.toLowerCase().includes(query);
    return matchesType && matchesKeyword;
  });
});

function showSlide(index) {
  if (!banners.value.length) return;
  currentSlide.value = (index + banners.value.length) % banners.value.length;
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
    [banners.value, journals.value, experts.value] = await Promise.all([api.publicBanners(), api.publicJournals(), api.publicExperts()]);
    startCarousel();
  } catch (error) {
    loadError.value = "官网数据加载失败，请确认后端服务已启动。";
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
onBeforeUnmount(() => clearInterval(carouselTimer));
</script>

<template>
  <div>
    <h1 class="visually-hidden">思研学术 SCI 特刊交流中心</h1>
    <section class="site-hero">
      <div class="shell">
        <div v-if="activeBanner" class="hero-image hero-carousel" @mouseenter="paused = true" @mouseleave="paused = false">
          <button v-if="previousBanner" class="hero-preview hero-preview-left" type="button" aria-label="预览上一张轮播图" @click="showSlide(currentSlide - 1)">
            <img :src="previousBanner.imageUrl" :alt="previousBanner.title" />
          </button>
          <button class="hero-arrow hero-arrow-left" aria-label="上一张" @click="showSlide(currentSlide - 1)">‹</button>
          <RouterLink class="hero-main-slide" to="/submit?subject=首页首屏&target=SCI">
            <img :src="activeBanner.imageUrl" :alt="activeBanner.title" />
            <span class="mobile-hero-caption">
              <b>{{ activeBanner.title }}</b>
              <small>提交稿件后，顾问将协助完成方向评估、期刊匹配与服务建议。</small>
            </span>
          </RouterLink>
          <button class="hero-arrow hero-arrow-right" aria-label="下一张" @click="showSlide(currentSlide + 1)">›</button>
          <button v-if="nextBanner" class="hero-preview hero-preview-right" type="button" aria-label="预览下一张轮播图" @click="showSlide(currentSlide + 1)">
            <img :src="nextBanner.imageUrl" :alt="nextBanner.title" />
          </button>
        </div>
        <div v-else-if="loading" class="hero-skeleton">轮播内容加载中…</div>
        <div v-else class="hero-skeleton error-state">{{ loadError }}<button class="ghost" @click="loadData">重新加载</button></div>
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
            <img class="advantage-icon" :src="`/images/advantage-icons/${item.icon}.png`" alt="" aria-hidden="true" />
          </article>
        </div>
        <div class="advantage-actions">
          <RouterLink class="primary" to="/submit?subject=学术服务发表优势&target=SCI">免费评估稿件</RouterLink>
        </div>
      </div>
    </section>

    <section class="section pale">
      <div class="shell solution-grid"><figure class="solution-cover"><img src="/images/sci-journals-books-cutout.png" alt="SCI 期刊封面组合" /></figure><div class="solution-copy"><h2 class="solution-title">可靠的SCI全流程解决方案</h2><p>论文写完了，发表才刚刚开始。选刊拿不准、流程摸不透、语言不过关、审稿意见不知如何回复——每一个环节都可能拖上数月。思研学术 SCI 特刊快速通道，将上述问题一并纳入标准服务流程：精准匹配已授权的正规特刊、编委团队前置审稿、专业编辑语言润色、审稿意见协同回应。</p><p>我们不替您写论文，不替您伪造审稿，只做一件事——帮您把合格的稿件，高效、合规地送进特刊的录用通道。</p><p>您负责把研究做扎实，我们把发表做简单。</p><div class="solution-actions"><RouterLink class="primary" to="/SCI">了解更多</RouterLink></div></div></div>
    </section>

    <section class="section shell">
      <div class="heading"><div><span>ACADEMIC SERVICES</span><h2>论文服务</h2></div><p>根据稿件所处阶段选择语言或科学编辑服务。</p></div>
      <div class="service-entry-grid"><RouterLink class="service-entry translation" to="/services/translation"><span>LANGUAGE SERVICES</span><h3>翻译润色</h3><p>高级翻译、母语润色与深度语言编辑。</p><b>查看服务 →</b></RouterLink><RouterLink class="service-entry editing" to="/services/editing"><span>SCIENTIFIC EDITING</span><h3>科学编辑</h3><p>稿件诊断、科学编辑与返修支持。</p><b>查看服务 →</b></RouterLink></div>
    </section>

    <section class="quick-service">
      <div class="shell service-points">
        <article><b>01</b><div><strong>正规期刊资源</strong><span>SCI / EI 多学科方向</span></div></article>
        <article><b>02</b><div><strong>稿件专业评估</strong><span>匹配方向与发表周期</span></div></article>
        <article><b>03</b><div><strong>编辑协同支持</strong><span>语言、结构与回复优化</span></div></article>
        <article><b>04</b><div><strong>流程清晰透明</strong><span>全程跟进关键节点</span></div></article>
      </div>
    </section>

    <section id="journals" class="section shell">
      <div class="heading"><div><span>JOURNAL CATALOG</span><h2>精选期刊方向</h2></div><p>选择类型或输入关键词，快速查找适合的研究方向。</p></div>
      <div class="journal-tools">
        <div class="segmented"><button v-for="type in ['ALL', 'SCI', 'EI']" :key="type" :class="{ active: journalType === type }" @click="journalType = type">{{ type === "ALL" ? "全部期刊" : type + " 期刊" }}</button></div>
        <input v-model="keyword" type="search" placeholder="搜索期刊名称、学科方向或关键词" />
      </div>
      <div v-if="loading" class="empty-state">期刊列表加载中…</div>
      <div v-else-if="filteredJournals.length" class="journal-grid">
        <article v-for="journal in filteredJournals" :key="journal.id">
          <span class="tag">{{ journal.type }} · {{ journal.field }}</span>
          <h3>{{ journal.title }}</h3>
          <p>{{ journal.description }}</p>
          <div><b>{{ journal.indexType }}</b><small>{{ journal.cycle }}</small></div>
          <RouterLink :to="`/${journal.type}/${journal.id}`">查看期刊详情 →</RouterLink>
        </article>
      </div>
      <div v-else class="empty-state"><b>没有匹配的期刊</b><span>调整筛选条件或提交稿件信息，由顾问协助选刊。</span></div>
    </section>

    <section id="submission" class="section pale home-assessment-band">
      <div class="shell">
        <div><span class="eyebrow">ONE CLEAR PATH</span><h2>从免费评估开始</h2><p>上传稿件或填写需求，顾问确认服务方案；登录学生账号后，可在订单中心持续查看进度与文件。</p></div>
        <div class="home-assessment-flow"><article v-for="(step, index) in submissionSteps" :key="step.title"><b>{{ String(index + 1).padStart(2, "0") }}</b><span>{{ step.title }}</span></article></div>
        <RouterLink class="primary" to="/submit?subject=首页统一投稿入口&target=SCI">免费评估稿件</RouterLink>
      </div>
    </section>

    <section class="section shell">
      <div class="heading"><div><span>GLOBAL EXPERTS</span><h2>专家团队，汇聚全球学者</h2></div><RouterLink to="/about">了解团队与机构 →</RouterLink></div>
      <div class="expert-grid">
        <article v-for="expert in experts" :key="expert.id" class="card"><img :src="expert.imageUrl" :alt="expert.name" /><div><h3>{{ expert.name }}</h3><p>{{ expert.institution }}</p><small>{{ expert.role }}</small></div></article>
      </div>
    </section>

    <section class="section pale"><div class="shell partner-block"><div><span class="eyebrow">PUBLISHER NETWORK</span><h2>国际期刊和出版社合作资源</h2><p>正式版本可将合作单位拆分为后台可维护的独立数据项，并增加资质与合作说明。</p><RouterLink class="ghost" to="/about">了解机构介绍</RouterLink></div><img src="/images/publisher-partners.jpg" alt="国际期刊和出版社合作资源" /></div></section>
  </div>
</template>
