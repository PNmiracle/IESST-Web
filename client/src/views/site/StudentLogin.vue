<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../../api";
import { studentSession } from "../../stores/studentSession";

const router = useRouter();
const route = useRoute();
const form = reactive({ username: "", password: "" });
const loading = ref(false);
const error = ref("");
const registerTarget = computed(() => {
  const query = {};
  const mobile = form.username.trim();
  if (mobile) query.mobile = mobile;
  if (typeof route.query.redirect === "string") query.redirect = route.query.redirect;
  return { name: "student-register", query };
});

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
      <h1>登录 / 注册</h1>
      <p>已有账号可直接登录；还没有账号时，请先创建学生账号，再查看稿件评估、服务咨询和处理进度。</p>
      <div v-if="error" class="inline-error">{{ error }}</div>
      <form @submit.prevent="login">
        <label>手机号<input v-model="form.username" inputmode="tel" autocomplete="username" required /></label>
        <label>密码<input v-model="form.password" type="password" autocomplete="current-password" required /></label>
        <button class="primary" :disabled="loading">{{ loading ? "正在登录…" : "登录并查看订单" }}</button>
      </form>
      <small class="auth-secondary">还没有账号？<RouterLink :to="registerTarget">立即注册</RouterLink></small>
      <small>忘记密码？短信验证重置功能正在接入中，请暂时联系顾问协助核验账号。</small>
    </div>
  </section>
</template>
