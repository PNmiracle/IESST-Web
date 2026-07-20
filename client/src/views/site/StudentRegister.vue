<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../../api";
import { studentSession } from "../../stores/studentSession";

const router = useRouter();
const route = useRoute();
const form = reactive({ mobile: "", displayName: "", password: "", confirmPassword: "" });
const loading = ref(false);
const error = ref("");

function redirectTarget() {
  const target = route.query.redirect;
  return typeof target === "string" && target.startsWith("/") && !target.startsWith("//")
    ? target
    : "/student/orders";
}

async function register() {
  if (loading.value) return;
  loading.value = true;
  error.value = "";
  try {
    studentSession.login(await api.studentRegister(form));
    router.replace(redirectTarget());
  } catch (exception) {
    error.value = exception.message || "注册失败";
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  if (typeof route.query.mobile === "string") form.mobile = route.query.mobile;
  if (await studentSession.restore()) router.replace(redirectTarget());
});
</script>

<template>
  <section class="student-page">
    <div class="student-auth-card card">
      <span class="eyebrow">QUICK REGISTER</span>
      <h1>快速注册</h1>
      <p>注册作者账号后，可登录查看自己的订单、服务进度和发票记录。</p>
      <div v-if="error" class="inline-error">{{ error }}</div>
      <form @submit.prevent="register">
        <label>手机号<input v-model="form.mobile" autocomplete="tel" placeholder="请输入 11 位手机号" required /></label>
        <label>姓名<input v-model="form.displayName" autocomplete="name" placeholder="用于订单和编辑沟通" required /></label>
        <label>登录密码<input v-model="form.password" type="password" autocomplete="new-password" minlength="8" maxlength="64" placeholder="8 至 64 位" required /></label>
        <label>确认密码<input v-model="form.confirmPassword" type="password" autocomplete="new-password" required /></label>
        <button class="primary" :disabled="loading">{{ loading ? "正在注册…" : "注册并进入订单中心" }}</button>
      </form>
      <small>已有账号？<RouterLink to="/student/login">去登录</RouterLink></small>
    </div>
  </section>
</template>
