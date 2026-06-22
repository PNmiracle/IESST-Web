<script setup>
import { onBeforeUnmount, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ConsultationModal from "../components/ConsultationModal.vue";
import { openConsultation } from "../stores/consultation";
import { studentSession } from "../stores/studentSession";

const route = useRoute();
const router = useRouter();

function consultHref(subject, targetType = "SCI") {
  const query = new URLSearchParams({ consult: "1", subject, targetType });
  return `${route.path}?${query.toString()}`;
}

function handleConsultClick(event) {
  const trigger = event.target.closest?.("[data-consult-subject]");
  if (!trigger) return;
  event.preventDefault();
  openConsultation({
    subject: trigger.dataset.consultSubject,
    targetType: trigger.dataset.consultTarget || "SCI",
    message: trigger.dataset.consultMessage || "",
  });
}

async function logoutStudent() {
  await studentSession.logout();
  if (route.path.startsWith("/student/")) router.replace("/student/login");
}

onMounted(() => {
  document.addEventListener("click", handleConsultClick);
  studentSession.restore();
});
onMounted(() => {
  window.__iesstConsultationFromElement = (trigger) => {
    openConsultation({
      subject: trigger.dataset.consultSubject,
      targetType: trigger.dataset.consultTarget || "SCI",
      message: trigger.dataset.consultMessage || "",
    });
  };
});
onBeforeUnmount(() => {
  document.removeEventListener("click", handleConsultClick);
  delete window.__iesstConsultationFromElement;
});

watch(
  () => route.query.consult,
  (value) => {
    if (!value) return;
    openConsultation({
      subject: route.query.subject || "预约一对一沟通",
      targetType: route.query.targetType || "SCI",
      message: route.query.message || "",
    });
    const query = { ...route.query };
    delete query.consult;
    delete query.subject;
    delete query.targetType;
    delete query.message;
    router.replace({ path: route.path, query, hash: route.hash });
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <header class="site-header">
      <div class="site-top">
        <div class="shell site-top-inner">
          <div>服务热线：0371-65867066</div>
          <nav class="site-top-links">
            <template v-if="studentSession.isLoggedIn.value">
              <RouterLink to="/student/orders">{{ studentSession.state.displayName || studentSession.state.mobile }}</RouterLink>
              <RouterLink to="/student/orders">我的订单</RouterLink>
              <button type="button" @click="logoutStudent">退出</button>
            </template>
            <template v-else>
              <RouterLink class="login-link" to="/student/login">请登录</RouterLink>
              <span class="divider">|</span>
              <RouterLink to="/student/register">快速注册</RouterLink>
            </template>
          </nav>
        </div>
      </div>
      <div class="shell site-nav">
        <RouterLink class="brand brand-logo" to="/" aria-label="IESST 思研学术首页">
          <img class="brand-mark" src="/images/logo-icon.png" alt="IESST" />
          <img class="brand-wordmark" src="/images/logo-wordmark.png" alt="IESST 思研学术 · SCI 特刊交流中心" />
        </RouterLink>
        <nav>
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/EI">EI期刊</RouterLink>
          <RouterLink to="/SCI">SCI期刊</RouterLink>
          <RouterLink to="/services/translation">翻译润色</RouterLink>
          <RouterLink to="/services/editing">科学编辑</RouterLink>
          <RouterLink to="/about">关于我们</RouterLink>
        </nav>
        <RouterLink class="primary compact" to="/submit">免费评估稿件</RouterLink>
      </div>
    </header>
    <main><RouterView /></main>
    <footer class="site-footer">
      <div class="shell site-footer-grid">
        <div class="brand light"><span>IESST</span><small>思研学术 · SCI 特刊交流中心</small></div>
        <nav><RouterLink to="/SCI">SCI期刊</RouterLink><RouterLink to="/EI">EI期刊</RouterLink><RouterLink to="/services/translation">翻译润色</RouterLink><RouterLink to="/services/editing">科学编辑</RouterLink></nav>
        <div><b>提交评估</b><p>上传稿件或填写需求</p><RouterLink class="footer-consult" to="/submit">开始免费评估 →</RouterLink></div>
      </div>
    </footer>
    <ConsultationModal />
  </div>
</template>
