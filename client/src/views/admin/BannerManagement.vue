<script setup>
import { onMounted, reactive, ref } from "vue";
import { api } from "../../api";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import { showNotice } from "../../stores/notice";

const items = ref([]);
const loading = ref(true);
const saving = ref(false);
const uploading = ref(false);
const loadError = ref("");
const deleting = ref(null);
const form = reactive(emptyForm());

function emptyForm() {
  return { id: null, title: "", imageUrl: "/images/optimized/hero-center-1200.webp", linkUrl: "/SCI", sortOrder: 1, enabled: true };
}

function resetForm() {
  Object.assign(form, emptyForm());
}

function edit(item) {
  Object.assign(form, item);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    items.value = await api.adminBanners();
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
    await api.saveBanner({ ...form });
    showNotice(form.id ? "轮播图修改成功" : "轮播图新增成功");
    resetForm();
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    saving.value = false;
  }
}

async function uploadImage(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  uploading.value = true;
  try {
    const result = await api.uploadImage(file);
    form.imageUrl = result.url;
    showNotice("图片上传成功");
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    uploading.value = false;
    event.target.value = "";
  }
}

async function confirmDelete() {
  try {
    await api.deleteBanner(deleting.value.id);
    showNotice("轮播图已删除");
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
    <header class="page-title"><div><span>BANNER MANAGEMENT</span><h1>轮播图管理</h1><p>维护访客官网首页轮播图、排序和启用状态。</p></div><button class="ghost" @click="resetForm">新增轮播图</button></header>
    <div class="admin-layout">
      <form class="card editor" @submit.prevent="save">
        <div class="editor-title"><div><span>{{ form.id ? "EDIT BANNER" : "NEW BANNER" }}</span><h2>{{ form.id ? "编辑轮播图" : "新增轮播图" }}</h2></div><button v-if="form.id" type="button" class="text-action" @click="resetForm">取消编辑</button></div>
        <label>标题<input v-model="form.title" required /></label>
        <label>上传轮播图片<input type="file" accept="image/png,image/jpeg,image/webp,image/gif" :disabled="uploading" @change="uploadImage" /><small>{{ uploading ? "正在上传…" : "支持 JPG、PNG、WebP、GIF，最大 10MB" }}</small></label>
        <label>图片地址<input v-model="form.imageUrl" required /></label>
        <label>跳转地址<input v-model="form.linkUrl" /></label>
        <label>显示排序<input v-model.number="form.sortOrder" type="number" min="1" /></label>
        <label class="check"><input v-model="form.enabled" type="checkbox" /> 在官网启用</label>
        <div class="preview"><span>图片预览</span><img :src="form.imageUrl" alt="" /></div>
        <button class="primary" :disabled="saving">{{ saving ? "正在保存…" : form.id ? "保存修改" : "新增轮播图" }}</button>
      </form>
      <div class="list">
        <div v-if="loading" class="empty-state">轮播图加载中…</div>
        <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
        <article v-for="item in items" v-else :key="item.id" class="card row">
          <img :src="item.imageUrl" :alt="item.title" />
          <div><b>{{ item.title }}</b><small>排序 {{ item.sortOrder }} · {{ item.enabled ? "官网已启用" : "官网未启用" }}</small></div>
          <button @click="edit(item)">编辑</button><button class="danger" @click="deleting = item">删除</button>
        </article>
        <div v-if="!loading && !loadError && !items.length" class="empty-state"><b>暂无轮播图</b><span>使用左侧表单添加第一张轮播图。</span></div>
      </div>
    </div>
    <ConfirmDialog v-if="deleting" title="删除轮播图？" :message="`将删除“${deleting.title}”，此操作无法撤销。`" @cancel="deleting = null" @confirm="confirmDelete" />
  </section>
</template>
