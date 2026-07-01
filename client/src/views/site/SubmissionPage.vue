<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";
import { studentSession } from "../../stores/studentSession";

const route = useRoute();
const submitting = ref(false);
const result = ref(null);
const uploadResult = ref(null);
const pendingSubmission = ref(null);
const pendingLinkedToOrder = ref(false);
const resultLinkedToOrder = ref(false);
const manuscriptFile = ref(null);
const manuscriptInput = ref(null);
let nextAuthorKey = 2;

const form = reactive({
  paperTitle: "",
  targetType: "SCI",
  serviceType: "高级翻译",
  expedited: false,
  contactName: "",
  contactEmail: "",
  contactPhone: "",
  specialRequirements: "",
  sourceMessage: "",
  authors: [newAuthor(1, true)],
});

const serviceOptions = ["高级翻译", "深度润色", "高级查重", "高级降重", "AI率控制", "其他服务"];
const isServiceMode = computed(() => route.query.mode === "service" || Boolean(route.query.serviceType));
const pageTitle = computed(() => isServiceMode.value ? "提交论文服务需求" : "论文提交");
const pageEyebrow = computed(() => isServiceMode.value ? "ACADEMIC SERVICE REQUEST" : "MANUSCRIPT SUBMISSION");

function newAuthor(key = nextAuthorKey++, correspondingAuthor = false) {
  return { key, name: "", email: "", institution: "", correspondingAuthor };
}

function addAuthor() {
  if (form.authors.length >= 20) {
    showNotice("作者人数不能超过20人", true);
    return;
  }
  form.authors.push(newAuthor());
}

function removeAuthor(index) {
  if (form.authors.length === 1) return;
  const removedWasCorresponding = form.authors[index].correspondingAuthor;
  form.authors.splice(index, 1);
  if (removedWasCorresponding) form.authors[0].correspondingAuthor = true;
}

function chooseCorresponding(index) {
  form.authors.forEach((author, authorIndex) => {
    author.correspondingAuthor = authorIndex === index;
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

function resetForm() {
  result.value = null;
  uploadResult.value = null;
  pendingSubmission.value = null;
  pendingLinkedToOrder.value = false;
  resultLinkedToOrder.value = false;
  manuscriptFile.value = null;
  if (manuscriptInput.value) manuscriptInput.value.value = "";
}

async function submit() {
  if (submitting.value) return;
  if (!manuscriptFile.value) {
    showNotice("请上传论文附件", true);
    manuscriptInput.value?.click();
    return;
  }

  const correspondingAuthor = form.authors.find((author) => author.correspondingAuthor);
  if (!isServiceMode.value && (!correspondingAuthor || !correspondingAuthor.email)) {
    showNotice("请选择通讯作者并填写其邮箱", true);
    return;
  }

  const contactName = isServiceMode.value ? form.contactName : correspondingAuthor.name;
  const contactEmail = isServiceMode.value ? form.contactEmail : correspondingAuthor.email;
  submitting.value = true;
  uploadResult.value = null;
  try {
    if (!pendingSubmission.value) {
      pendingLinkedToOrder.value = await studentSession.restore(true);
    }
    const saved = pendingSubmission.value || await api.submit({
      id: null,
      authorName: contactName,
      email: contactEmail,
      paperTitle: form.paperTitle,
      targetType: isServiceMode.value ? (route.query.target || "翻译润色") : form.targetType,
      message: form.sourceMessage,
      serviceType: isServiceMode.value ? form.serviceType : null,
      expedited: isServiceMode.value ? form.expedited : false,
      contactPhone: isServiceMode.value ? form.contactPhone : null,
      specialRequirements: form.specialRequirements,
      authors: isServiceMode.value ? [] : form.authors.map((author, index) => ({
        id: null,
        name: author.name,
        email: author.email,
        institution: author.institution,
        correspondingAuthor: author.correspondingAuthor,
        sortOrder: index,
      })),
      status: null,
      createdAt: null,
    });
    pendingSubmission.value = saved;
    uploadResult.value = await api.uploadSubmissionFile(saved.id, saved.uploadToken, manuscriptFile.value);
    result.value = saved;
    resultLinkedToOrder.value = pendingLinkedToOrder.value;
    pendingSubmission.value = null;
    pendingLinkedToOrder.value = false;
    showNotice("论文信息与附件已提交，后台记录已同步");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  if (route.query.target) form.targetType = route.query.target;
  if (route.query.serviceType) form.serviceType = route.query.serviceType;
  if (route.query.subject) form.sourceMessage = `提交来源：${route.query.subject}`;
  const isReady = await studentSession.restore();
  if (isReady) {
    form.contactName = studentSession.state.displayName || "";
    form.authors[0].name = studentSession.state.displayName || "";
  }
});
</script>

<template>
  <section class="page-hero assessment-hero">
    <div class="shell">
      <span>{{ pageEyebrow }}</span>
      <h1>{{ pageTitle }}</h1>
    </div>
  </section>

  <section class="section shell submit-page-grid unified-submit-page">
    <section v-if="result" class="card submit-success">
      <span>✓</span>
      <h3>提交成功</h3>
      <p>记录编号：#{{ result.id }}，当前状态：{{ result.status }}。附件已同步：{{ uploadResult?.fileName }}。</p>
      <p v-if="!resultLinkedToOrder" class="submit-success-note">本次为未登录提交，不会显示在订单中心。下次可先登录账号，再提交并持续查看处理进度。</p>
      <div class="submit-success-actions">
        <RouterLink v-if="resultLinkedToOrder" class="primary" to="/student/orders">查看我的订单</RouterLink>
        <RouterLink v-else class="primary" :to="{ path: '/student/login', query: { redirect: '/submit' } }">登录 / 注册</RouterLink>
        <button class="ghost" type="button" @click="resetForm">继续提交</button>
      </div>
    </section>

    <form v-else class="card form-grid manuscript-submission-form" @submit.prevent="submit">
      <header class="submission-form-heading wide">
        <span>{{ isServiceMode ? "SERVICE DETAILS" : "MANUSCRIPT DETAILS" }}</span>
        <h2>{{ isServiceMode ? "填写服务需求" : "填写论文信息" }}</h2>
        <p>带 * 的项目为必填项，提交后由编辑进行初步确认。</p>
      </header>

      <label class="wide">论文题目 *<input v-model="form.paperTitle" required maxlength="500" placeholder="请输入完整论文题目" /></label>

      <label class="wide manuscript-upload-field">
        上传附件 *
        <input ref="manuscriptInput" type="file" required accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" @change="chooseManuscript" />
        <small>支持 PDF、DOC、DOCX，单文件不超过 20MB。</small>
      </label>

      <template v-if="isServiceMode">
        <label>服务类型 *
          <select v-model="form.serviceType" required>
            <option v-for="option in serviceOptions" :key="option">{{ option }}</option>
          </select>
        </label>
        <label class="expedited-field">是否加急
          <button type="button" class="expedited-toggle" :class="{ active: form.expedited }" :aria-pressed="form.expedited" @click="form.expedited = !form.expedited">
            <span></span>{{ form.expedited ? "加急处理" : "常规处理" }}
          </button>
        </label>
        <label>联系人 *<input v-model="form.contactName" required maxlength="255" placeholder="请输入联系人姓名" /></label>
        <label>联系邮箱 *<input v-model="form.contactEmail" type="email" required maxlength="255" placeholder="用于接收服务反馈" /></label>
        <label class="wide">手机号 *<input v-model="form.contactPhone" type="tel" required maxlength="50" placeholder="请输入联系人手机号" /></label>
      </template>

      <section v-else class="submission-authors wide">
        <div class="submission-authors-heading">
          <div><span>AUTHOR INFORMATION</span><h2>作者信息</h2><p>可动态添加作者，并指定一位通讯作者。</p></div>
          <button class="ghost" type="button" @click="addAuthor">＋ 添加作者</button>
        </div>
        <article v-for="(author, index) in form.authors" :key="author.key" class="submission-author-card">
          <header><b>作者 {{ index + 1 }}</b><button v-if="form.authors.length > 1" type="button" aria-label="删除作者" @click="removeAuthor(index)">删除</button></header>
          <label>姓名 *<input v-model="author.name" required maxlength="255" placeholder="请输入作者姓名" /></label>
          <label>单位<input v-model="author.institution" maxlength="500" placeholder="学校、医院或研究机构" /></label>
          <label class="author-email"><span class="author-email-label">邮箱 <b v-if="author.correspondingAuthor">*</b></span><input v-model="author.email" :required="author.correspondingAuthor" type="email" maxlength="255" placeholder="请输入作者邮箱" /></label>
          <label class="corresponding-choice"><input type="radio" name="corresponding-author" :checked="author.correspondingAuthor" @change="chooseCorresponding(index)" />设为通讯作者</label>
        </article>
      </section>

      <label class="wide">特殊要求<textarea v-model="form.specialRequirements" maxlength="5000" placeholder="可填写目标期刊、交付时间、语言要求或其他说明"></textarea></label>
      <button class="primary wide submission-final-button" :disabled="submitting">{{ submitting ? "正在提交…" : isServiceMode ? "提交服务需求" : "提交论文" }}</button>
    </form>
  </section>
</template>
