<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../../api";
import { session } from "../../stores/session";

const route = useRoute();
const router = useRouter();
const form = reactive({ username: "admin", password: "" });
const loading = ref(false);
const error = ref("");

async function login() {
  if (loading.value) return;
  loading.value = true;
  error.value = "";
  try {
    session.login(await api.login(form));
    router.replace(typeof route.query.redirect === "string" ? route.query.redirect : "/admin/dashboard");
  } catch (exception) {
    error.value = exception.message;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (route.query.expired) error.value = "登录状态已失效，请重新登录。";
});
</script>

<template>
  <main class="login-page">
    <section class="login-aside"><div class="brand light"><span>IESST</span><small>思研学术管理后台</small></div><div><span class="eyebrow">CONTENT MANAGEMENT</span><h1>让内容维护更清晰、更高效</h1><p>独立管理轮播图、SCI / EI 期刊和用户投稿记录。</p></div><a href="/">← 返回访客官网</a></section>
    <section class="login-panel">
      <form class="login-card" @submit.prevent="login">
        <span class="eyebrow">ADMIN LOGIN</span><h2>管理员登录</h2><p>请输入管理员账号和密码进入内容管理后台。</p>
        <div v-if="error" class="inline-error">{{ error }}</div>
        <label>账号<input v-model="form.username" autocomplete="username" /></label>
        <label>密码<input v-model="form.password" type="password" autocomplete="current-password" /></label>
        <button class="primary" :disabled="loading">{{ loading ? "正在登录…" : "登录后台" }}</button>
      </form>
    </section>
  </main>
</template>
