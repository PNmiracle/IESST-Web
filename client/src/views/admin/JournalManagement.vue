<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { api } from "../../api";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import { showNotice } from "../../stores/notice";

const items = ref([]);
const loading = ref(true);
const saving = ref(false);
const imageUploading = ref(false);
const documentUploading = ref(false);
const loadError = ref("");
const deleting = ref(null);
const filter = ref("ALL");
const form = reactive(emptyForm());
const filteredItems = computed(() => filter.value === "ALL" ? items.value : items.value.filter((item) => item.type === filter.value));

function emptyForm() {
  return {
    id: null,
    type: "SCI",
    title: "",
    field: "",
    indexType: "",
    cycle: "",
    description: "",
    imageUrl: "",
    journalLevel: "",
    impactFactorLabel: "",
    impactFactorValue: "",
    journalPartition: "",
    acceptanceTime: "",
    submissionDeadlineText: "长期征稿",
    submissionDeadlineDate: "",
    disciplineCategory: "",
    casZone: "",
    jcrQuartile: "",
    viewCount: 0,
    documentName: "",
    documentUrl: "",
    published: true,
  };
}

function resetForm() {
  Object.assign(form, emptyForm());
}

function edit(item) {
  Object.assign(form, item);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function normalizeNumber(value) {
  return value === "" || value === null || value === undefined ? null : Number(value);
}

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    items.value = await api.adminJournals();
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (saving.value) return;
  saving.value = true;
  try {
    await api.saveJournal({ ...form, impactFactorValue: normalizeNumber(form.impactFactorValue) });
    showNotice(form.id ? "期刊修改成功" : "期刊新增成功");
    resetForm();
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    saving.value = false;
  }
}

async function uploadDocument(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  documentUploading.value = true;
  try {
    const result = await api.uploadDocument(file);
    form.documentName = result.fileName;
    form.documentUrl = result.url;
    showNotice("期刊文档上传成功");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    documentUploading.value = false;
    event.target.value = "";
  }
}

async function uploadImage(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  imageUploading.value = true;
  try {
    const result = await api.uploadImage(file);
    form.imageUrl = result.url;
    showNotice("期刊图片上传成功");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    imageUploading.value = false;
    event.target.value = "";
  }
}

async function confirmDelete() {
  try {
    await api.deleteJournal(deleting.value.id);
    showNotice("期刊已删除");
    deleting.value = null;
    await load();
  } catch (error) {
    showNotice(error.message, true);
  }
}

onMounted(load);
</script>

<template>
  <section class="admin-page">
    <header class="page-title"><div><span>JOURNAL MANAGEMENT</span><h1>期刊管理</h1><p>维护 SCI / EI 期刊信息与官网上架状态。</p></div><div class="segmented"><button v-for="type in ['ALL', 'SCI', 'EI']" :key="type" :class="{ active: filter === type }" @click="filter = type">{{ type === "ALL" ? "全部" : type }}</button></div></header>
    <div class="admin-layout">
      <form class="card editor" @submit.prevent="save">
        <div class="editor-title"><div><span>{{ form.id ? "EDIT JOURNAL" : "NEW JOURNAL" }}</span><h2>{{ form.id ? "编辑期刊" : "新增期刊" }}</h2></div><button v-if="form.id" type="button" class="text-action" @click="resetForm">取消编辑</button></div>
        <label>类型<select v-model="form.type"><option>SCI</option><option>EI</option><option>SCOPUS</option></select></label>
        <label>期刊名称<input v-model="form.title" required /></label>
        <label>学科方向<input v-model="form.field" required /></label>
        <label>学科领域<select v-model="form.disciplineCategory"><option value="">请选择</option><option>土木建筑工程</option><option>交通物流工程</option><option>环境科学工程</option><option>机械电子工程</option><option>计算机信息工程</option><option>材料科学</option><option>数学物理</option><option>化学工程</option><option>生物学</option><option>医学</option></select></label>
        <label>期刊级别<input v-model="form.journalLevel" placeholder="SCI / SSCI / EI" /></label>
        <label>检索类型<input v-model="form.indexType" placeholder="SCI / EI Compendex" /></label>
        <label>影响因子展示<input v-model="form.impactFactorLabel" placeholder="1.5+ / 0-6 / -" /></label>
        <label>影响因子数值<input v-model="form.impactFactorValue" type="number" min="0" step="0.001" placeholder="用于排序和区间筛选" /></label>
        <label>期刊分区<input v-model="form.journalPartition" placeholder="4区，Q3" /></label>
        <label>中科院<select v-model="form.casZone"><option value="">请选择</option><option>1区</option><option>2区</option><option>3区</option><option>4区</option><option>-</option></select></label>
        <label>JCR<select v-model="form.jcrQuartile"><option value="">请选择</option><option>Q1</option><option>Q2</option><option>Q3</option><option>Q4</option><option>-</option></select></label>
        <label>录用时间<input v-model="form.acceptanceTime" placeholder="预计1-2个月" /></label>
        <label>预计周期<input v-model="form.cycle" /></label>
        <label>截稿时间文案<input v-model="form.submissionDeadlineText" placeholder="长期征稿" /></label>
        <label>截稿日期<input v-model="form.submissionDeadlineDate" type="date" /></label>
        <label class="wide">期刊简介<textarea v-model="form.description"></textarea></label>
        <label class="wide">上传期刊图片<input type="file" accept="image/png,image/jpeg,image/webp,image/gif" :disabled="imageUploading" @change="uploadImage" /><small>{{ imageUploading ? "正在上传…" : "支持 JPG、PNG、WebP，建议 600x800 或 4:3 封面图" }}</small></label>
        <div v-if="form.imageUrl" class="uploaded-preview wide"><img :src="form.imageUrl" alt="期刊图片预览" /><button type="button" class="text-action" @click="form.imageUrl=''">移除图片</button></div>
        <label class="wide">上传期刊文档<input type="file" accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt" :disabled="documentUploading" @change="uploadDocument" /><small>{{ documentUploading ? "正在上传…" : "支持 PDF、Word、Excel、PPT，最大 20MB" }}</small></label>
        <div v-if="form.documentUrl" class="uploaded-file"><a :href="form.documentUrl" target="_blank">{{ form.documentName || "查看已上传文档" }}</a><button type="button" class="text-action" @click="form.documentName='';form.documentUrl=''">移除</button></div>
        <label class="check"><input v-model="form.published" type="checkbox" /> 在官网上架展示</label>
        <button class="primary" :disabled="saving">{{ saving ? "正在保存…" : form.id ? "保存修改" : "新增期刊" }}</button>
      </form>
      <div class="list">
        <div v-if="loading" class="empty-state">期刊列表加载中…</div>
        <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
        <article v-for="item in filteredItems" v-else :key="item.id" class="card row journal-row journal-admin-row">
          <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
          <div v-else class="journal-admin-placeholder">{{ item.type }}</div>
          <div><span class="tag">{{ item.type }}</span><b>{{ item.title }}</b><small>{{ item.disciplineCategory || item.field }} · {{ item.journalLevel || item.indexType || "未设置级别" }} · {{ item.impactFactorLabel || "影响因子未设置" }} · {{ item.published ? "官网已上架" : "官网未上架" }}<template v-if="item.documentName"> · 已上传文档</template></small></div>
          <button @click="edit(item)">编辑</button><button class="danger" @click="deleting = item">删除</button>
        </article>
        <div v-if="!loading && !loadError && !filteredItems.length" class="empty-state"><b>没有匹配的期刊</b><span>切换筛选类型或新增期刊。</span></div>
      </div>
    </div>
    <ConfirmDialog v-if="deleting" title="删除期刊？" :message="`将删除“${deleting.title}”，此操作无法撤销。`" @cancel="deleting = null" @confirm="confirmDelete" />
  </section>
</template>
