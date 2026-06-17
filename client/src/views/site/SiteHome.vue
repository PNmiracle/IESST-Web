<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const banners = ref([]);
const journals = ref([]);
const experts = ref([]);
const currentSlide = ref(0);
const loading = ref(true);
const loadError = ref("");
const paused = ref(false);
const journalType = ref("ALL");
const keyword = ref("");
const submitting = ref(false);
const submitted = ref(false);
const submissionForm = reactive({ authorName: "", email: "", paperTitle: "", targetType: "SCI", message: "" });
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

async function submitPaper() {
  if (submitting.value) return;
  submitting.value = true;
  try {
    await api.submit({ ...submissionForm, id: null, status: null, createdAt: null });
    submitted.value = true;
    Object.assign(submissionForm, { authorName: "", email: "", paperTitle: "", targetType: "SCI", message: "" });
    showNotice("投稿信息已成功提交");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    submitting.value = false;
  }
}

onMounted(loadData);
onBeforeUnmount(() => clearInterval(carouselTimer));
</script>

<template>
  <div>
    <section class="site-hero">
      <div class="shell">
        <div v-if="activeBanner" class="hero-image hero-carousel" @mouseenter="paused = true" @mouseleave="paused = false">
          <button v-if="previousBanner" class="hero-preview hero-preview-left" type="button" aria-label="预览上一张轮播图" @click="showSlide(currentSlide - 1)">
            <img :src="previousBanner.imageUrl" :alt="previousBanner.title" />
          </button>
          <button class="hero-arrow hero-arrow-left" aria-label="上一张" @click="showSlide(currentSlide - 1)">‹</button>
          <RouterLink class="hero-main-slide" :to="activeBanner.linkUrl || '/SCI'">
            <img :src="activeBanner.imageUrl" :alt="activeBanner.title" />
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

    <section class="section shell">
      <div class="heading"><div><span>WHY IESST</span><h2>学术服务发表优势</h2></div><p>把复杂的发表流程拆成可理解、可选择、可跟进的服务节点。</p></div>
      <div class="advantage-grid">
        <article v-for="(item,index) in ['权威期刊资源','专业编辑团队','高效发表流程','高被引支持','编辑一对一咨询','隐私安全保障']" :key="item" class="card"><b>0{{ index + 1 }}</b><h3>{{ item }}</h3><p>{{ ['覆盖 SCI / EI 多学科方向，按研究主题和周期筛选。','由具备学科背景的编辑参与评估、语言与质量复核。','明确服务节点和交付内容，持续同步处理进展。','围绕摘要、关键词和研究亮点优化学术表达。','结合稿件阶段提供清晰、可执行的建议。','严格管理稿件、作者资料与沟通记录。'][index] }}</p></article>
      </div>
    </section>

    <section class="section pale">
      <div class="shell solution-grid"><figure class="solution-cover"><img src="/images/sci-journals-books-cutout.png" alt="SCI 期刊封面组合" /></figure><div><span class="eyebrow">FULL-CYCLE SOLUTION</span><h2>可靠的 SCI 全流程解决方案</h2><p>从稿件初评、期刊匹配、语言与科学编辑，到投稿材料和审稿回复协同，形成清晰的服务路径。</p><ol><li><b>01</b> 稿件初评与研究方向识别</li><li><b>02</b> 综合范围、检索与周期匹配期刊</li><li><b>03</b> 优化语言、结构与科学表达</li><li><b>04</b> 协同准备材料与审稿回复</li></ol><div class="detail-actions"><RouterLink class="primary" to="/SCI">查看 SCI 期刊</RouterLink><a class="ghost" href="?consult=1&subject=SCI%E5%85%A8%E6%B5%81%E7%A8%8B%E6%96%B9%E6%A1%88&targetType=SCI" data-consult-subject="SCI全流程方案" data-consult-target="SCI" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">了解更多</a></div></div></div>
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

    <section id="submission" class="section pale">
      <div class="shell submit-layout">
        <div><span class="eyebrow">ONLINE SUBMISSION</span><h2>提交稿件信息，获取初步建议</h2><p>填写研究方向、稿件标题与联系方式。管理员会在后台收到记录，并持续更新处理状态。</p><ul><li>稿件信息仅用于服务评估</li><li>支持 SCI 与 EI 方向咨询</li><li>工作时间内尽快回复</li></ul></div>
        <section v-if="submitted" class="card submit-success"><span>✓</span><h3>投稿信息已收到</h3><p>管理员后台已生成新的投稿记录，我们会尽快与您联系。</p><button class="ghost" @click="submitted = false">继续提交另一篇稿件</button></section>
        <form v-else class="card form-grid" @submit.prevent="submitPaper">
          <label>作者姓名<input v-model="submissionForm.authorName" required /></label>
          <label>联系邮箱<input v-model="submissionForm.email" type="email" required /></label>
          <label class="wide">论文标题<input v-model="submissionForm.paperTitle" required /></label>
          <label>目标类型<select v-model="submissionForm.targetType"><option>SCI</option><option>EI</option></select></label>
          <label>补充说明<input v-model="submissionForm.message" placeholder="研究方向、当前阶段等" /></label>
          <button class="primary wide" :disabled="submitting">{{ submitting ? "正在提交…" : "提交稿件信息" }}</button>
        </form>
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
