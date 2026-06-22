<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../../api";
import { studentSession } from "../../stores/studentSession";

const router = useRouter();
const route = useRoute();
const form = reactive({ username: "", password: "" });
const loading = ref(false);
const error = ref("");

async function login() {
  if (loading.value) return;
  loading.value = true;
  error.value = "";
  try {
    studentSession.login(await api.studentLogin(form));
    router.replace(typeof route.query.redirect === "string" ? route.query.redirect : "/student/orders");
  } catch (exception) {
    error.value = exception.message || "登录失败";
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  if (await studentSession.restore()) router.replace("/student/orders");
});
</script>

<template>
  <section class="student-page">
    <div class="student-auth-card card">
      <span class="eyebrow">STUDENT CENTER</span>
      <h1>学生登录</h1>
      <p>登录后可查看自己提交过的稿件评估、服务咨询和处理进度。</p>
      <div v-if="error" class="inline-error">{{ error }}</div>
      <form @submit.prevent="login">
        <label>手机号<input v-model="form.username" inputmode="tel" autocomplete="username" required /></label>
        <label>密码<input v-model="form.password" type="password" autocomplete="current-password" required /></label>
        <button class="primary" :disabled="loading">{{ loading ? "正在登录…" : "登录并查看订单" }}</button>
      </form>
      <small>忘记密码？短信验证重置功能正在接入中，请暂时联系顾问协助核验账号。</small>
    </div>
  </section>
</template>
