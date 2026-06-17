<script setup>
import { reactive, ref, watch } from "vue";
import { api } from "../api";
import { closeConsultation, consultation } from "../stores/consultation";
import { showNotice } from "../stores/notice";

const submitting = ref(false);
const done = ref(false);
const form = reactive({
  authorName: "",
  email: "",
  paperTitle: "",
  targetType: "SCI",
  message: "",
});

function reset(payload = {}) {
  done.value = false;
  Object.assign(form, {
    authorName: "",
    email: "",
    paperTitle: payload.subject || "",
    targetType: payload.targetType || payload.target || "SCI",
    message: payload.message || "",
  });
}

function show(payload = {}) {
  reset(payload);
}

function close() {
  if (submitting.value) return;
  closeConsultation();
}

async function submit() {
  if (submitting.value) return;
  submitting.value = true;
  try {
    await api.submit({ ...form, id: null, status: null, createdAt: null });
    done.value = true;
    showNotice("咨询信息已提交，后台已生成跟进记录");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    submitting.value = false;
  }
}

watch(() => consultation.seed, () => show(consultation.payload), { immediate: true });
</script>

<template>
  <Teleport to="body">
    <div v-if="consultation.open" class="consult-backdrop" @click.self="close">
      <section class="consult-modal card" role="dialog" aria-modal="true" aria-labelledby="consult-title">
        <button class="consult-close" type="button" aria-label="关闭弹窗" @click="close">×</button>
        <template v-if="done">
          <div class="consult-success"><span>✓</span><h2 id="consult-title">信息已收到</h2><p>顾问会根据您填写的内容进行初步判断，后台投稿记录中也会同步生成一条待跟进记录。</p><button class="primary" type="button" @click="close">好的</button></div>
        </template>
        <template v-else>
          <div class="consult-heading"><span>CONTACT CONSULTANT</span><h2 id="consult-title">预约一对一沟通</h2><p>留下基础信息即可，不会跳转离开当前页面。</p></div>
          <form class="consult-form" @submit.prevent="submit">
            <label>作者姓名<input v-model="form.authorName" required placeholder="请输入姓名" /></label>
            <label>联系邮箱<input v-model="form.email" type="email" required placeholder="用于接收顾问回复" /></label>
            <label class="wide">咨询主题<input v-model="form.paperTitle" required placeholder="例如 SCI 全流程方案 / 翻译润色 / 期刊适配" /></label>
            <label>目标类型<select v-model="form.targetType"><option>SCI</option><option>EI</option><option>翻译润色</option><option>科学编辑</option></select></label>
            <label class="wide">补充说明<textarea v-model="form.message" placeholder="可填写研究方向、当前稿件阶段、希望解决的问题"></textarea></label>
            <div class="consult-actions"><button class="ghost" type="button" @click="close">取消</button><button class="primary" type="submit" :disabled="submitting">{{ submitting ? "正在提交…" : "提交咨询" }}</button></div>
          </form>
        </template>
      </section>
    </div>
  </Teleport>
</template>
