<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";
import { studentSession } from "../../stores/studentSession";

const route = useRoute();
const submitting = ref(false);
const result = ref(null);
const uploadResult = ref(null);
const manuscriptFile = ref(null);
const manuscriptInput = ref(null);
const form = reactive({
  authorName: "",
  email: "",
  paperTitle: "",
  targetType: "SCI",
  message: "",
});
const conversionSteps = [
  { title: "免费评估稿件", text: "上传稿件或填写当前需求，系统生成后台记录。" },
  { title: "顾问确认方案", text: "结合方向、服务类型和稿件阶段判断处理路径。" },
  { title: "同步处理进度", text: "登录学生账号提交后，可在我的订单查看节点。" },
];

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

function resetForm() {
  result.value = null;
  uploadResult.value = null;
  manuscriptFile.value = null;
  if (manuscriptInput.value) manuscriptInput.value.value = "";
}

async function submit() {
  if (submitting.value) return;
  submitting.value = true;
  uploadResult.value = null;
  try {
    const saved = await api.submit({ ...form, id: null, status: null, createdAt: null });
    result.value = saved;
    if (manuscriptFile.value) {
      uploadResult.value = await api.uploadSubmissionFile(saved.id, saved.uploadToken, manuscriptFile.value);
    }
    showNotice(manuscriptFile.value ? "稿件与需求已提交，后台记录已同步" : "评估需求已成功提交");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  if (route.query.target) form.targetType = route.query.target;
  if (route.query.subject) form.message = `评估来源：${route.query.subject}`;
  const isReady = await studentSession.restore();
  if (isReady) {
    form.authorName = studentSession.state.displayName || "";
  }
});
</script>

<template>
  <section class="page-hero assessment-hero">
    <div class="shell">
      <span>FREE ASSESSMENT</span>
      <h1>免费评估稿件</h1>
      <p>统一入口：上传稿件或填写需求，后台生成记录；登录学生账号后同步生成订单进度。</p>
    </div>
  </section>
  <section class="section shell submit-page-grid unified-submit-page">
    <div>
      <h2>一条清晰的提交路径</h2>
      <p class="submit-page-lead">不需要先判断该点“咨询”还是“提交”。先完成免费评估，顾问会根据稿件阶段推荐 SCI/EI、翻译润色或科学编辑服务。</p>
      <div class="submission-flow">
        <article v-for="(step, index) in conversionSteps" :key="step.title">
          <b>{{ String(index + 1).padStart(2, "0") }}</b>
          <div><strong>{{ step.title }}</strong><span>{{ step.text }}</span></div>
        </article>
      </div>
      <div class="card privacy-note">
        <b>隐私说明</b>
        <p>提交信息与附件仅用于稿件评估。学生登录状态下提交，会自动同步一条可追踪的订单记录。</p>
      </div>
    </div>
    <section v-if="result" class="card submit-success">
      <span>✓</span>
      <h3>评估需求已收到</h3>
      <p>记录编号：#{{ result.id }}，当前状态：{{ result.status }}。{{ uploadResult ? `附件已同步：${uploadResult.fileName}。` : "未上传附件，顾问会先根据需求说明评估。" }}</p>
      <ol>
        <li v-for="step in conversionSteps" :key="step.title">{{ step.title }}</li>
      </ol>
      <div class="submit-success-actions">
        <RouterLink class="primary" to="/student/orders">查看我的订单</RouterLink>
        <button class="ghost" type="button" @click="resetForm">继续提交</button>
      </div>
    </section>
    <form v-else class="card form-grid" @submit.prevent="submit">
      <label>作者姓名<input v-model="form.authorName" required placeholder="请输入姓名" /></label>
      <label>联系邮箱<input v-model="form.email" type="email" required placeholder="用于接收评估反馈" /></label>
      <label class="wide">论文标题<input v-model="form.paperTitle" required placeholder="请输入论文标题或暂定题目" /></label>
      <label>目标类型<select v-model="form.targetType"><option>SCI</option><option>EI</option><option>翻译润色</option><option>科学编辑</option></select></label>
      <label class="manuscript-upload-field">
        稿件文件（可选）
        <input ref="manuscriptInput" type="file" accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" @change="chooseManuscript" />
        <small>支持 PDF、DOC、DOCX，单文件不超过 20MB。</small>
      </label>
      <label class="wide">需求说明<textarea v-model="form.message" placeholder="可填写研究方向、当前阶段、目标期刊、希望解决的问题"></textarea></label>
      <button class="primary wide" :disabled="submitting">{{ submitting ? "正在提交…" : "提交免费评估" }}</button>
    </form>
  </section>
</template>
