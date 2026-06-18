<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";
import { studentSession } from "../../stores/studentSession";

const route = useRoute();
const items = ref([]);
const submitting = ref(false);
const submitDialogOpen = ref(false);
const submitResult = ref(null);
const manuscriptFile = ref(null);
const manuscriptInput = ref(null);
const submitForm = ref({
  authorName: "",
  email: "",
  paperTitle: "",
  message: "",
  planTitle: "",
});
const meta = {
  translation: { eyebrow:"LANGUAGE SERVICES",title:"论文翻译/润色服务",navTitle:"翻译润色",subtitle:"让研究成果以准确、自然、符合国际期刊规范的语言呈现。",image:"/images/translation-service-visual.jpg",highlights:["9大学科领域的翻译团队，学术背景深厚","2000+专家，深耕学术翻译","严格的质量控制体系，确保您的研究领域成果忠于原文"] },
  editing: { eyebrow:"SCIENTIFIC EDITING",title:"论文科学编辑/极速出版服务",navTitle:"科学编辑",subtitle:"从稿件诊断、科学编辑到出版准备，帮助作者清晰呈现研究价值。",image:"/images/scientific-editing-service-visual.png",highlights:["9大学科领域编辑团队，学术背景深厚","2000+学科专家，深耕科学编辑","严格质量控制，确保研究成果忠于原意"] },
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

function consultHref(subject, targetType) {
  const query = new URLSearchParams({ consult: "1", subject, targetType });
  return `${route.path}?${query.toString()}`;
}
function consultMessage(item) {
  if (!item) return `咨询来源：${service.value.title}顶部入口`;
  return [`咨询套餐：${item.title}`, `价格说明：${item.price}`, item.description].filter(Boolean).join("\n");
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
function openSubmitDialog(item = null) {
  submitResult.value = null;
  manuscriptFile.value = null;
  submitForm.value = {
    authorName: studentSession.state.displayName || "",
    email: "",
    paperTitle: "",
    planTitle: item?.title || "",
    message: [
      `提交来源：${service.value.title}`,
      item ? `意向服务：${item.title}（${item.price}）` : "",
      studentSession.state.mobile ? `学生账号：${studentSession.state.mobile}` : "",
    ].filter(Boolean).join("\n"),
  };
  submitDialogOpen.value = true;
  studentSession.restore().then(() => {
    if (!submitDialogOpen.value) return;
    if (!submitForm.value.authorName) {
      submitForm.value.authorName = studentSession.state.displayName || "";
    }
    if (studentSession.state.mobile && !submitForm.value.message.includes("学生账号：")) {
      submitForm.value.message = [submitForm.value.message, `学生账号：${studentSession.state.mobile}`].filter(Boolean).join("\n");
    }
  });
}
function chooseManuscript(event) {
  const file = event.target.files?.[0] || null;
  if (!file) {
    manuscriptFile.value = null;
    return;
  }
  const extension = file.name.split(".").pop()?.toLowerCase();
  if (!["pdf", "doc", "docx"].includes(extension)) {
    event.target.value = "";
    manuscriptFile.value = null;
    showNotice("仅支持 PDF、DOC、DOCX 文件", true);
    return;
  }
  if (file.size > 20 * 1024 * 1024) {
    event.target.value = "";
    manuscriptFile.value = null;
    showNotice("稿件文件不能超过 20MB", true);
    return;
  }
  manuscriptFile.value = file;
}
function closeSubmitDialog() {
  if (!submitting.value) submitDialogOpen.value = false;
}
async function submitServiceRequest() {
  if (submitting.value) return;
  if (!manuscriptFile.value) {
    showNotice("请选择需要上传的 PDF 或 Word 稿件", true);
    manuscriptInput.value?.click();
    return;
  }
  submitting.value = true;
  try {
    const submission = await api.submit({
      id: null,
      authorName: submitForm.value.authorName,
      email: submitForm.value.email,
      paperTitle: submitForm.value.paperTitle,
      targetType: serviceTarget.value,
      message: submitForm.value.message,
      status: null,
      createdAt: null,
    });
    const upload = await api.uploadSubmissionFile(
      submission.id,
      submitForm.value.email,
      manuscriptFile.value,
    );
    submitResult.value = { ...submission, upload };
    showNotice("稿件与文件已上传，后台及订单记录已同步");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    submitting.value = false;
  }
}
onMounted(async () => {
  try {
    items.value = await api.publicServices();
  } catch (error) {
    showNotice(error.message, true);
  }
  studentSession.restore();
});
</script>

<template>
  <section v-if="isPremiumService" class="section shell translation-intro-section">
    <article class="translation-intro-card">
      <figure :class="['translation-visual', { 'editing-visual': isEditing }]">
        <img :src="service.image" :alt="service.title" />
        <figcaption v-if="isEditing"><b>论文科学编辑</b><b>极速出版支持</b></figcaption>
      </figure>
      <div class="translation-intro-copy">
        <h1>{{ service.title }}</h1>
        <ul>
          <li v-for="item in service.highlights" :key="item">{{ item }}</li>
        </ul>
        <div class="translation-intro-actions">
          <button class="primary submit-button" type="button" @click="openSubmitDialog()">提交稿件</button>
          <a class="primary consult-button" :href="consultHref(service.title, serviceTarget)" :data-consult-subject="service.title" :data-consult-target="serviceTarget" :data-consult-message="consultMessage()" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">咨询客服</a>
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
      <a class="primary" :href="consultHref(service.title, service.title)" :data-consult-subject="service.title" :data-consult-target="service.title" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">获取服务方案</a>
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
        <a :href="consultHref(`${service.title}-${item.title}`, serviceTarget)" :data-consult-subject="`${service.title}-${item.title}`" :data-consult-target="serviceTarget" :data-consult-message="`咨询科学编辑能力：${item.title}\n${item.description}`" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">点击了解</a>
      </article>
    </div>
  </section>
  <section class="section shell service-list-section">
    <div v-if="!isPremiumService" class="journal-tools service-tools">
      <div><span class="tag">{{ service.navTitle || service.title }}专区</span></div>
      <div class="service-highlight-inline">
        <span v-for="item in service.highlights" :key="item">✓ {{ item }}</span>
      </div>
    </div>
    <div class="heading service-type-heading">
      <div><h2>服务类型</h2></div>
      <p v-if="!isPremiumService">根据稿件阶段和实际需求选择服务，提交后由顾问进一步确认范围。</p>
    </div>
    <div :class="['service-card-grid', { 'translation-pricing-grid': isTranslation, 'editing-pricing-grid': isEditing }]"><article v-for="item in displayItems" :key="item.id" :class="['card service-card', { 'translation-plan-card': isPremiumService, 'editing-plan-card': isEditing }]"><header><h3>{{ item.title }}</h3><span>{{ item.price }}</span></header><p>{{ item.description }}</p><ul><li v-for="feature in item.features.split('\n').filter(Boolean)" :key="feature">{{ feature }}</li></ul><div class="translation-plan-actions"><a class="primary" :href="consultHref(`${service.title}-${item.title}`, serviceTarget)" :data-consult-subject="`${service.title}-${item.title}`" :data-consult-target="serviceTarget" :data-consult-message="consultMessage(item)" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">立即咨询</a></div></article></div>
  </section>
  <Teleport to="body">
    <div v-if="submitDialogOpen" class="translation-submit-backdrop" @click.self="closeSubmitDialog">
      <section class="translation-submit-modal card" role="dialog" aria-modal="true" aria-labelledby="translation-submit-title">
        <button class="consult-close" type="button" aria-label="关闭弹窗" @click="closeSubmitDialog">×</button>
        <template v-if="submitResult">
          <div class="translation-submit-success">
            <span>✓</span>
            <h2 id="translation-submit-title">稿件信息已提交</h2>
            <p>记录编号：#{{ submitResult.id }}，文件：{{ submitResult.upload?.fileName }}。管理员后台已生成投稿与附件记录，登录学生提交时会同步生成订单进度。</p>
            <RouterLink v-if="studentSession.isLoggedIn.value" class="primary" to="/student/orders" @click="closeSubmitDialog">查看我的订单</RouterLink>
            <button v-else class="primary" type="button" @click="closeSubmitDialog">好的</button>
          </div>
        </template>
        <template v-else>
          <div class="consult-heading">
            <span>{{ isEditing ? "SCIENTIFIC EDITING SUBMISSION" : "TRANSLATION SUBMISSION" }}</span>
            <h2 id="translation-submit-title">提交{{ serviceTarget }}稿件</h2>
            <p>提交后会写入后台投稿记录；学生登录状态下同步生成可追踪订单。</p>
          </div>
          <form class="consult-form" @submit.prevent="submitServiceRequest">
            <label>作者姓名<input v-model="submitForm.authorName" required placeholder="请输入姓名" /></label>
            <label>联系邮箱<input v-model="submitForm.email" type="email" required placeholder="用于接收服务回复" /></label>
            <label class="wide">论文标题<input v-model="submitForm.paperTitle" required placeholder="请输入论文标题或稿件名称" /></label>
            <label v-if="submitForm.planTitle">意向服务<input v-model="submitForm.planTitle" disabled /></label>
            <label class="wide manuscript-upload-field">
              稿件文件
              <input ref="manuscriptInput" type="file" required accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" @change="chooseManuscript" />
              <small>支持 PDF、DOC、DOCX，单文件不超过 20MB。</small>
            </label>
            <label class="wide">补充说明<textarea v-model="submitForm.message" placeholder="可填写研究方向、语种、字数、当前阶段等"></textarea></label>
            <div class="consult-actions"><button class="ghost" type="button" @click="closeSubmitDialog">取消</button><button class="primary" type="submit" :disabled="submitting">{{ submitting ? "正在提交…" : "提交并同步后台" }}</button></div>
          </form>
        </template>
      </section>
    </div>
  </Teleport>
</template>
