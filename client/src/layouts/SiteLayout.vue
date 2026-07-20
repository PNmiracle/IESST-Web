<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ConsultationModal from "../components/ConsultationModal.vue";
import { openConsultation } from "../stores/consultation";
import { studentSession } from "../stores/studentSession";

const route = useRoute();
const router = useRouter();
const mobileMenuOpen = ref(false);

function syncPreviewMode() {
  const forceMobile = route.query.preview === "mobile";
  document.documentElement.classList.toggle("mobile-preview", forceMobile);
}

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
  syncPreviewMode();
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
  document.documentElement.classList.remove("mobile-preview");
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

watch(
  () => route.fullPath,
  () => {
    mobileMenuOpen.value = false;
    syncPreviewMode();
  },
);
</script>

<template>
  <div class="site-app">
    <header class="site-header">
      <div class="shell site-nav">
        <RouterLink class="brand brand-logo" to="/" aria-label="IESST 思研学术首页">
          <img class="brand-primary-logo" src="/images/iesst-logo-primary-compact.png" alt="IESST 思研学术 · 核心期刊交流中心" />
        </RouterLink>
        <div class="mobile-header-contact">服务热线：0371-65867066</div>
        <button class="mobile-menu-toggle" type="button" :aria-expanded="mobileMenuOpen" aria-label="打开导航菜单" @click="mobileMenuOpen = !mobileMenuOpen">
          <span></span><span></span><span></span>
        </button>
        <nav :class="['site-main-menu', { open: mobileMenuOpen }]" aria-label="主导航">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/EI">EI期刊</RouterLink>
          <RouterLink to="/SCI">SCI期刊</RouterLink>
          <RouterLink to="/services/translation">翻译润色</RouterLink>
          <RouterLink to="/services/editing">科学编辑</RouterLink>
          <RouterLink :to="{ path: '/', hash: '#scholar-recruitment' }">学者招募</RouterLink>
          <RouterLink to="/about">关于我们</RouterLink>
        </nav>
        <div class="site-nav-actions">
          <RouterLink class="primary compact" to="/submit">提交稿件评估</RouterLink>
          <nav class="site-auth-links" aria-label="学生账号">
            <template v-if="studentSession.isLoggedIn.value">
              <RouterLink to="/student/orders">{{ studentSession.state.displayName || studentSession.state.mobile }}</RouterLink>
              <RouterLink to="/student/orders">我的订单</RouterLink>
              <button type="button" @click="logoutStudent">退出</button>
            </template>
            <template v-else>
              <RouterLink class="login-link" to="/student/login">登录/注册</RouterLink>
            </template>
          </nav>
        </div>
      </div>
    </header>
    <main><RouterView /></main>
    <footer class="site-footer">
      <div class="shell site-footer-main">
        <RouterLink class="footer-brand-logo" to="/" aria-label="IESST 思研学术首页">
          <img src="/images/iesst-logo-primary-compact.png" alt="IESST 思研学术 · 核心期刊交流中心" />
        </RouterLink>
        <nav class="footer-link-column" aria-label="我们的服务">
          <b>我们的服务</b>
          <RouterLink to="/SCI">SCI期刊</RouterLink>
          <RouterLink to="/EI">EI期刊</RouterLink>
          <RouterLink to="/services/translation">翻译润色</RouterLink>
          <RouterLink to="/services/editing">科学编辑</RouterLink>
        </nav>
        <nav class="footer-link-column" aria-label="关于 IESST">
          <b>关于 IESST</b>
          <RouterLink to="/about">机构介绍</RouterLink>
          <RouterLink :to="{ path: route.path, hash: '#site-contact' }">联系我们</RouterLink>
          <RouterLink to="/about">合作申请</RouterLink>
        </nav>
        <nav class="footer-link-column" aria-label="专家服务">
          <b>专家服务</b>
          <RouterLink to="/about">专家团队</RouterLink>
          <RouterLink to="/services/editing">审稿合作</RouterLink>
          <RouterLink to="/services/editing">编辑支持</RouterLink>
        </nav>
        <nav class="footer-link-column" aria-label="帮助中心">
          <b>帮助中心</b>
          <RouterLink to="/submit">稿件评估</RouterLink>
          <RouterLink to="/student/login">登录/注册</RouterLink>
          <RouterLink to="/student/orders">我的订单</RouterLink>
        </nav>
      </div>
      <aside id="site-contact" class="shell site-footer-contact" aria-labelledby="site-contact-title">
        <div class="site-footer-contact-copy">
          <h2 id="site-contact-title"><span aria-hidden="true"></span>联系我们</h2>
          <h3>项目合作</h3>
          <strong>IESST 思研学术合作中心</strong>
          <p>电话/微信：<a href="tel:037165867066">0371-65867066</a></p>
          <p>邮箱：<a href="mailto:editor@iesst.com">editor@iesst.com</a></p>
        </div>
        <div class="site-footer-contact-qr">
          <img src="/images/editor-contact-qr.png" alt="编辑咨询二维码" loading="lazy" decoding="async" />
          <span>扫一扫添加</span>
        </div>
      </aside>
      <div class="site-footer-bottom"><div class="shell">Copyright © 2019–2026 IESST 思研学术 · SCI 特刊交流中心</div></div>
    </footer>
    <RouterLink
      class="author-support-ad"
      :to="{ path: '/submit', query: { subject: '优秀作者扶持计划', target: 'SCI', support: '1' }, hash: '#author-support-program' }"
      aria-label="优秀作者扶持计划火热报名中，立即申请"
    >
      <span aria-hidden="true">优</span>
      <div><strong>优秀作者扶持计划</strong><b>火热报名中！</b></div>
      <em>立即申请 →</em>
    </RouterLink>
    <ConsultationModal />
  </div>
</template>
