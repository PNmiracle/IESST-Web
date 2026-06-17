<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const items = ref([]);
const loading = ref(true);
const loadError = ref("");
const updatingId = ref(null);
const exporting = ref(false);
const filters = reactive({ status: "全部", keyword: "", page: 1, size: 20 });
const pagination = reactive({ total: 0, totalPages: 0 });
let searchTimer = null;

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    const result = await api.submissions(filters);
    items.value = result.items;
    pagination.total = result.total;
    pagination.totalPages = result.totalPages;
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

function chooseStatus(status) {
  filters.status = status;
  filters.page = 1;
  load();
}

function changePage(page) {
  if (page < 1 || page > pagination.totalPages || page === filters.page) return;
  filters.page = page;
  load();
}

async function updateStatus(id, status) {
  updatingId.value = id;
  try {
    await api.updateStatus(id, status);
    showNotice("投稿状态已更新");
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    updatingId.value = null;
  }
}

async function exportCsv() {
  exporting.value = true;
  try {
    const blob = await api.exportSubmissions({ status: filters.status, keyword: filters.keyword });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `submissions-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    showNotice("投稿记录已导出");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    exporting.value = false;
  }
}

watch(() => filters.keyword, () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    filters.page = 1;
    load();
  }, 350);
});

onMounted(load);
</script>

<template>
  <section class="admin-page">
    <header class="page-title">
      <div><span>SUBMISSION MANAGEMENT</span><h1>投稿记录</h1><p>服务端分页检索用户投稿，并持续更新沟通状态。</p></div>
      <div class="submission-tools">
        <input v-model.trim="filters.keyword" type="search" placeholder="搜索作者、邮箱或论文标题" />
        <div class="segmented"><button v-for="status in ['全部', '待处理', '沟通中', '已完成']" :key="status" :class="{ active: filters.status === status }" @click="chooseStatus(status)">{{ status }}</button></div>
        <button class="ghost" :disabled="exporting" @click="exportCsv">{{ exporting ? "正在导出…" : "导出 CSV" }}</button>
      </div>
    </header>
    <div v-if="loading" class="empty-state">投稿记录加载中…</div>
    <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
    <section v-else-if="items.length" class="card table-wrap">
      <table><thead><tr><th>作者</th><th>论文与说明</th><th>目标</th><th>联系方式</th><th>提交时间</th><th>处理状态</th></tr></thead><tbody><tr v-for="item in items" :key="item.id"><td><b>{{ item.authorName }}</b></td><td><b>{{ item.paperTitle }}</b><small>{{ item.message || "未填写补充说明" }}</small></td><td><span class="tag">{{ item.targetType }}</span></td><td>{{ item.email }}</td><td>{{ item.createdAt?.replace("T", " ").slice(0, 16) }}</td><td><select :disabled="updatingId === item.id" :value="item.status" @change="updateStatus(item.id, $event.target.value)"><option>待处理</option><option>沟通中</option><option>已完成</option></select></td></tr></tbody></table>
      <footer class="table-pagination">
        <span>共 {{ pagination.total }} 条，第 {{ filters.page }} / {{ pagination.totalPages }} 页</span>
        <div><button class="ghost" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">上一页</button><button class="ghost" :disabled="filters.page >= pagination.totalPages" @click="changePage(filters.page + 1)">下一页</button></div>
      </footer>
    </section>
    <div v-else class="empty-state"><b>没有匹配的投稿记录</b><span>调整关键词或状态筛选后重试。</span></div>
  </section>
</template>
