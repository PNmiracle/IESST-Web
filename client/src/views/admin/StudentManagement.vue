<script setup>
import { computed, onMounted, ref } from "vue";
import { api } from "../../api";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import { showNotice } from "../../stores/notice";

const items = ref([]);
const loading = ref(true);
const loadError = ref("");
const statusFilter = ref("all");
const changing = ref(null);
const confirming = ref(null);
const filteredItems = computed(() => items.value.filter((item) => {
  if (statusFilter.value === "enabled") return item.enabled;
  if (statusFilter.value === "disabled") return !item.enabled;
  return true;
}));

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    items.value = await api.adminStudents();
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function confirmStatusChange() {
  const request = confirming.value;
  if (!request) return;
  changing.value = request.item.id;
  try {
    await api.setStudentEnabled(request.item.id, request.enabled);
    showNotice(request.enabled ? "学生账号已启用" : "学生账号已停用");
    confirming.value = null;
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    changing.value = null;
  }
}

onMounted(load);
</script>

<template>
  <section class="admin-page">
    <header class="page-title student-account-head">
      <div><span>STUDENT ACCOUNTS</span><h1>学生账号管理</h1><p>学生自行注册账号，后台仅负责查看状态与控制登录权限。</p></div>
      <div class="segmented" aria-label="账号状态筛选">
        <button v-for="option in [{ value: 'all', label: '全部' }, { value: 'enabled', label: '已启用' }, { value: 'disabled', label: '已停用' }]" :key="option.value" :class="{ active: statusFilter === option.value }" @click="statusFilter = option.value">{{ option.label }}</button>
      </div>
    </header>

    <aside class="privacy-notice">
      <span aria-hidden="true">隐</span>
      <div><b>敏感信息受保护</b><p>手机号仅向已登录管理员显示，数据库采用加密存储；查看列表和变更账号状态都会写入安全审计日志。</p></div>
    </aside>

    <div class="student-account-list">
        <div v-if="loading" class="empty-state">学生账号加载中…</div>
        <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
        <article v-for="item in filteredItems" v-else :key="item.id" class="card student-account-row">
          <div class="student-identity"><span>{{ item.displayName?.slice(0, 1) || "学" }}</span><div><b>{{ item.displayName }}</b><small>账号 ID：{{ item.id }}</small></div></div>
          <div class="student-mobile"><small>登录手机号</small><b>{{ item.mobile }}</b></div>
          <span class="account-status" :class="{ disabled: !item.enabled }">{{ item.enabled ? "已启用" : "已停用" }}</span>
          <button :class="item.enabled ? 'danger-outline' : 'ghost'" :disabled="changing === item.id" @click="confirming = { item, enabled: !item.enabled }">{{ changing === item.id ? "处理中…" : item.enabled ? "停用登录" : "恢复登录" }}</button>
        </article>
        <div v-if="!loading && !loadError && !filteredItems.length" class="empty-state"><b>暂无匹配账号</b><span>学生完成注册后，账号会自动出现在这里。</span></div>
    </div>
    <ConfirmDialog v-if="confirming" :title="confirming.enabled ? '恢复学生登录？' : '停用学生登录？'" :message="confirming.enabled ? `恢复后，“${confirming.item.displayName}”可以重新登录并查看自己的订单。` : `停用后，“${confirming.item.displayName}”的现有会话将失效，但订单和历史记录会完整保留。`" @cancel="confirming = null" @confirm="confirmStatusChange" />
  </section>
</template>
