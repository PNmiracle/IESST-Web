<script setup>
import { useRouter } from "vue-router";
import { session } from "../stores/session";

const router = useRouter();

async function logout() {
  await session.logout();
  router.replace({ name: "admin-login" });
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink class="admin-brand-logo" to="/admin/dashboard" aria-label="IESST 思研学术管理后台"><img src="/images/iesst-logo-primary.png" alt="IESST 思研学术" /></RouterLink>
      <nav>
        <RouterLink to="/admin/dashboard"><b>览</b><span>数据概览</span></RouterLink>
        <RouterLink to="/admin/banners"><b>轮</b><span>轮播图管理</span></RouterLink>
        <RouterLink to="/admin/journals"><b>刊</b><span>期刊管理</span></RouterLink>
        <RouterLink to="/admin/submissions"><b>稿</b><span>投稿记录</span></RouterLink>
        <RouterLink to="/admin/students"><b>学</b><span>学生账号</span></RouterLink>
        <RouterLink to="/admin/orders"><b>单</b><span>订单发票</span></RouterLink>
        <RouterLink to="/admin/content"><b>文</b><span>官网内容</span></RouterLink>
        <RouterLink to="/admin/governance"><b>安</b><span>安全与审计</span></RouterLink>
      </nav>
      <div class="admin-user"><span>{{ session.state.displayName }}</span><button @click="logout">退出登录</button></div>
    </aside>
    <main class="admin-main">
      <header class="admin-top"><div><span>IESST ADMIN</span><b>管理后台</b></div><a href="/" target="_blank" rel="noreferrer">查看访客官网 ↗</a></header>
      <RouterView />
    </main>
  </div>
</template>
