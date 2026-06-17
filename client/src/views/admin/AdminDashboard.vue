<script setup>
import { computed, onMounted, ref } from "vue";
import { api } from "../../api";

const summary = ref(null);
const loading = ref(true);
const loadError = ref("");

const stats = computed(() => summary.value ? [
  { label: "轮播图", value: summary.value.enabledBannerCount, total: summary.value.bannerCount, unit: "张启用", to: "/admin/banners", tone: "purple" },
  { label: "期刊资源", value: summary.value.publishedJournalCount, total: summary.value.journalCount, unit: "本上架", to: "/admin/journals", tone: "blue" },
  { label: "投稿记录", value: summary.value.submissionCount, total: summary.value.submissionCount, unit: "条累计", to: "/admin/submissions", tone: "green" },
  { label: "待处理", value: summary.value.pendingSubmissionCount, total: summary.value.submissionCount, unit: "条待跟进", to: "/admin/submissions", tone: "orange" },
] : []);

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    summary.value = await api.dashboard();
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="admin-page dashboard-page">
    <header class="page-title">
      <div><span>OVERVIEW</span><h1>数据概览</h1><p>快速查看官网内容状态和最新投稿进展。</p></div>
      <RouterLink class="ghost" to="/">查看访客官网 ↗</RouterLink>
    </header>

    <div v-if="loading" class="empty-state">概览数据加载中…</div>
    <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
    <template v-else>
      <section class="dashboard-stats">
        <RouterLink v-for="item in stats" :key="item.label" :to="item.to" class="card stat-card" :class="`stat-${item.tone}`">
          <span>{{ item.label }}</span><strong>{{ item.value }}</strong>
          <small>{{ item.unit }} · 共 {{ item.total }}</small><b>查看详情 →</b>
        </RouterLink>
      </section>

      <section class="dashboard-grid">
        <article class="card dashboard-panel">
          <header><div><span>LATEST SUBMISSIONS</span><h2>最新投稿</h2></div><RouterLink to="/admin/submissions">查看全部 →</RouterLink></header>
          <div v-if="summary.latestSubmissions.length" class="latest-list">
            <RouterLink v-for="item in summary.latestSubmissions" :key="item.id" to="/admin/submissions">
              <div><b>{{ item.paperTitle }}</b><small>{{ item.authorName }} · {{ item.email }}</small></div>
              <div><span class="tag">{{ item.targetType }}</span><small>{{ item.status }}</small></div>
            </RouterLink>
          </div>
          <div v-else class="empty-state"><b>暂无投稿</b><span>访客提交后会显示在这里。</span></div>
        </article>

        <aside class="card dashboard-panel quick-actions">
          <header><div><span>QUICK ACTIONS</span><h2>快捷操作</h2></div></header>
          <RouterLink to="/admin/banners"><b>更新首页轮播</b><span>调整标题、图片和展示排序</span></RouterLink>
          <RouterLink to="/admin/journals"><b>新增期刊资源</b><span>维护 SCI / EI 期刊及上架状态</span></RouterLink>
          <RouterLink to="/admin/submissions"><b>处理投稿咨询</b><span>跟进待处理和沟通中的投稿</span></RouterLink>
        </aside>
      </section>
    </template>
  </section>
</template>
