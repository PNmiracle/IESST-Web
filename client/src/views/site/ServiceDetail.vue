<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const route = useRoute();
const items = ref([]);
const meta = {
  translation: { eyebrow:"LANGUAGE SERVICES",title:"论文翻译/润色服务",navTitle:"翻译润色",subtitle:"让研究成果以准确、自然、符合国际期刊规范的语言呈现。",image:"/images/translation-service-visual.jpg",highlights:["9大学科领域的翻译团队，学术背景深厚","2000+专家，深耕学术翻译","严格的质量控制体系，确保您的研究领域成果忠于原文"] },
  editing: { eyebrow:"SCIENTIFIC EDITING",title:"论文科学编辑/极速出版服务",navTitle:"科学编辑",subtitle:"从稿件诊断、科学编辑到出版准备，帮助作者清晰呈现研究价值。",image:"/images/optimized/scientific-editing-service-visual-1000.webp",highlights:["9大学科领域编辑团队，学术背景深厚","2000+学科专家，深耕科学编辑","严格质量控制，确保研究成果忠于原意"] },
};
const service = computed(() => ({ ...(meta[route.params.kind] || meta.translation), items:items.value.filter(item => item.category === route.params.kind) }));
const isTranslation = computed(() => route.params.kind === "translation");
const isEditing = computed(() => route.params.kind === "editing");
const isPremiumService = computed(() => isTranslation.value || isEditing.value);
const serviceTarget = computed(() => service.value.navTitle || service.value.title);
const editingCapabilities = [
  { number: "01", title: "高被引保障", description: "精准传递学术思想，由学科编辑与编委双重审核，优化研究叙事与国际期刊表达。" },
  { number: "02", title: "论文润色", description: "深度打磨术语、句式结构与逻辑层次，全面提升稿件的可读性和专业说服力。" },
  { number: "03", title: "查重降重", description: "采用国际主流查重系统定位高危重复内容，由专业编辑进行语义级改写并保留研究原意。" },
  { number: "04", title: "AI率控制", description: "检测疑似 AI 写作痕迹，通过人工改写与结构重组，降低可识别 AI 率并保持自然连贯。" },
];
const translationPlanContent = {
  高级翻译: {
    price: "¥0.8/字",
    description: "学科领域双语专家翻译，母语编辑润色，并进行质量审核。",
    features: ["资深译员首轮翻译", "学科领域双语专家审核", "标准母语编辑润色", "润色质量审核", "15天内1次免费修订", "翻译语言多轮修订服务"],
  },
  深度润色: {
    price: "¥0.5/词",
    description: "围绕术语、语法、逻辑与风格进行深度校修，确保上下文流畅。",
    features: ["专业术语拼写、用法编校", "语法错误全面检查与修正", "词汇修改与句子结构调整", "写作风格优化，确保上下文流畅", "文章逻辑与表述审核", "整体结构检查与修改", "90天内两轮修改服务", "免费提供润色证明"],
  },
};
const editingPlanContent = {
  稿件诊断: {
    title: "高级查重",
    price: "¥80/次",
    description: "国际主流查重系统检测，定位重复内容并生成详细报告。",
    features: ["国际主流查重系统（iThenticate）检测", "生成详细相似度报告，标红高危重复段落", "比对海量期刊、会议及网络数据库", "区分文献引用与实质性重复", "24小时内出具查重报告", "支持中英文双语稿件"],
  },
  科学编辑: {
    title: "高级降重",
    price: "¥0.35/字",
    description: "基于查重报告进行人工语义改写，降低重复率并保留专业原意。",
    features: ["基于查重报告定位高危重复内容", "语义级人工改写，保留原意与专业术语", "调整句式结构、语序及表达方式", "规避简单同义词替换，确保行文自然", "降重后再次查重验证，提供前后对比报告", "14天内1次免费修订"],
  },
  返修支持: {
    title: "AI率控制",
    price: "¥0.35/字",
    description: "检测并人工改写疑似 AI 内容，帮助稿件符合期刊出版伦理要求。",
    features: ["主流AI检测工具联合扫描", "精准识别AI生成段落及疑似内容", "人工逐句改写与结构重组，保留学术原意", "降低AI可识别率至期刊安全阈值以下", "提供修改前后AI检测对比报告", "10天内1次免费修订"],
  },
};
const displayItems = computed(() => service.value.items.map((item) => {
  const override = isTranslation.value ? translationPlanContent[item.title] : editingPlanContent[item.title];
  if (!override) return item;
  return { ...item, ...override, features: override.features.join("\n") };
}));

function submissionLink(item = null) {
  return {
    path: "/submit",
    query: {
      mode: "service",
      target: serviceTarget.value,
      serviceType: item?.title || undefined,
      subject: item ? `${service.value.title}-${item.title}` : service.value.title,
    },
  };
}

async function copyPageUrl() {
  const fallbackCopy = () => {
    const field = document.createElement("textarea");
    field.value = window.location.href;
    field.style.position = "fixed";
    field.style.opacity = "0";
    document.body.appendChild(field);
    field.select();
    document.execCommand("copy");
    field.remove();
  };
  try {
    if (!navigator.clipboard?.writeText) throw new Error("clipboard unavailable");
    await navigator.clipboard.writeText(window.location.href);
  } catch {
    fallbackCopy();
  }
  showNotice("页面地址已复制");
}
onMounted(async () => {
  try {
    items.value = await api.publicServices();
  } catch (error) {
    showNotice(error.message, true);
  }
});
</script>

<template>
  <section v-if="isPremiumService" class="section shell translation-intro-section">
    <article class="translation-intro-card">
      <figure :class="['translation-visual', { 'editing-visual': isEditing }]">
        <img :src="service.image" :alt="service.title" loading="eager" fetchpriority="high" decoding="async" />
        <figcaption v-if="isEditing"><b>论文科学编辑</b><b>极速出版支持</b></figcaption>
      </figure>
      <div class="translation-intro-copy">
        <h1>{{ service.title }}</h1>
        <ul>
          <li v-for="item in service.highlights" :key="item">{{ item }}</li>
        </ul>
        <div class="translation-intro-actions">
          <RouterLink class="primary submit-button" :to="submissionLink()">提交服务需求</RouterLink>
        </div>
      </div>
      <div class="translation-share">
        <span>分享页面：</span>
        <button type="button" @click="copyPageUrl">复制页面地址</button>
      </div>
    </article>
  </section>
  <section v-else class="page-hero service-page-hero">
    <div class="shell">
      <span>{{ service.eyebrow }}</span>
      <h1>{{ service.title }}</h1>
      <p>{{ service.subtitle }}</p>
      <RouterLink class="primary" :to="submissionLink()">提交服务需求</RouterLink>
    </div>
  </section>
  <section v-if="isEditing" class="section shell editing-capability-section">
    <div class="heading service-type-heading"><div><h2>论文科学编辑服务</h2></div></div>
    <div class="editing-proof-strip"><span v-for="item in service.highlights" :key="item">✓ {{ item }}</span></div>
    <div class="editing-capability-grid">
      <article v-for="item in editingCapabilities" :key="item.number" class="editing-capability-card">
        <span>{{ item.number }}</span>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
        <RouterLink :to="submissionLink(item)">提交服务需求</RouterLink>
      </article>
    </div>
  </section>
  <section :class="['section shell service-list-section', { 'editing-list-section': isEditing }]">
    <div v-if="!isPremiumService" class="journal-tools service-tools">
      <div><span class="tag">{{ service.navTitle || service.title }}专区</span></div>
      <div class="service-highlight-inline">
        <span v-for="item in service.highlights" :key="item">✓ {{ item }}</span>
      </div>
    </div>
    <div class="heading service-type-heading">
      <div><h2>服务类型</h2></div>
      <p v-if="!isPremiumService">根据稿件阶段和实际需求选择服务，提交后由编辑进一步确认范围。</p>
    </div>
    <div :class="['service-card-grid', { 'translation-pricing-grid': isTranslation, 'editing-pricing-grid': isEditing }]"><article v-for="item in displayItems" :key="item.id" :class="['card service-card', { 'translation-plan-card': isPremiumService, 'editing-plan-card': isEditing }]"><header><h3>{{ item.title }}</h3><span>{{ item.price }}</span></header><p>{{ item.description }}</p><ul><li v-for="feature in item.features.split('\n').filter(Boolean)" :key="feature">{{ feature }}</li></ul><div class="translation-plan-actions"><RouterLink class="primary" :to="submissionLink(item)">提交服务需求</RouterLink></div></article></div>
  </section>
</template>
