<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const form = reactive({ currentPassword: "", newPassword: "", confirmPassword: "" });
const saving = ref(false);
const logs = ref([]);
const loading = ref(true);
const loadError = ref("");
const filters = reactive({ action: "全部", keyword: "", page: 1, size: 20 });
const pagination = reactive({ total: 0, totalPages: 0 });
let searchTimer = null;

async function loadLogs() {
  loading.value = true;
  loadError.value = "";
  try {
    const result = await api.auditLogs(filters);
    logs.value = result.items;
    pagination.total = result.total;
    pagination.totalPages = result.totalPages;
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function changePassword() {
  saving.value = true;
  try {
    await api.changePassword(form);
    Object.assign(form, { currentPassword: "", newPassword: "", confirmPassword: "" });
    showNotice("管理员密码已更新");
    await loadLogs();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    saving.value = false;
  }
}

function actionLabel(action) {
  return { POST: "新增", PUT: "更新", PATCH: "更新", DELETE: "删除", READ_SENSITIVE: "敏感查看" }[action] || action;
}

function chooseAction(action) {
  filters.action = action;
  filters.page = 1;
  loadLogs();
}

function changePage(page) {
  if (page < 1 || page > pagination.totalPages || page === filters.page) return;
  filters.page = page;
  loadLogs();
}

watch(() => filters.keyword, () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    filters.page = 1;
    loadLogs();
  }, 350);
});

onMounted(loadLogs);
</script>

<template>
  <section class="admin-page governance-page">
    <header class="page-title">
      <div><span>SECURITY & AUDIT</span><h1>安全与审计</h1><p>维护管理员凭据，并追踪后台关键写操作。</p></div>
      <div class="submission-tools">
        <input v-model.trim="filters.keyword" type="search" placeholder="搜索账号、路径或 IP" />
        <div class="segmented"><button v-for="action in ['全部', 'READ_SENSITIVE', 'POST', 'PUT', 'DELETE']" :key="action" :class="{ active: filters.action === action }" @click="chooseAction(action)">{{ action === "全部" ? "全部" : actionLabel(action) }}</button></div>
        <button class="ghost" :disabled="loading" @click="loadLogs">刷新日志</button>
      </div>
    </header>

    <section class="governance-grid">
      <form class="card editor security-card" @submit.prevent="changePassword">
        <div class="editor-title"><div><span>ACCOUNT SECURITY</span><h2>修改登录密码</h2></div></div>
        <p class="form-hint">新密码至少 8 位。修改成功后，当前登录会话保持有效。</p>
        <label>当前密码<input v-model="form.currentPassword" type="password" autocomplete="current-password" required /></label>
        <label>新密码<input v-model="form.newPassword" type="password" autocomplete="new-password" minlength="8" required /></label>
        <label>确认新密码<input v-model="form.confirmPassword" type="password" autocomplete="new-password" minlength="8" required /></label>
        <button class="primary" :disabled="saving">{{ saving ? "正在更新…" : "更新密码" }}</button>
      </form>

      <article class="card audit-card">
        <header><div><span>OPERATION AUDIT</span><h2>最近操作</h2></div><small>共 {{ pagination.total }} 条</small></header>
        <div v-if="loading" class="empty-state">审计日志加载中…</div>
        <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}</div>
        <div v-else-if="logs.length" class="audit-list">
          <div v-for="item in logs" :key="item.id" class="audit-row">
            <span class="audit-action" :class="`action-${item.action.toLowerCase()}`">{{ actionLabel(item.action) }}</span>
            <div><b>{{ item.resourcePath }}</b><small>{{ item.username }} · {{ item.ipAddress || "未知地址" }}</small></div>
            <div><span :class="{ success: item.responseStatus < 400 }">HTTP {{ item.responseStatus }}</span><small>{{ item.createdAt?.replace("T", " ").slice(0, 19) }}</small></div>
          </div>
          <footer class="table-pagination">
            <span>第 {{ filters.page }} / {{ pagination.totalPages }} 页</span>
            <div><button class="ghost" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">上一页</button><button class="ghost" :disabled="filters.page >= pagination.totalPages" @click="changePage(filters.page + 1)">下一页</button></div>
          </footer>
        </div>
        <div v-else class="empty-state"><b>暂无操作记录</b><span>新增、修改或删除后台内容后会记录在这里。</span></div>
      </article>
    </section>
  </section>
</template>
