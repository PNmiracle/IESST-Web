<script setup>
import { onMounted, reactive, ref } from "vue";
import { api } from "../../api";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import { showNotice } from "../../stores/notice";

const items = ref([]);
const loading = ref(true);
const saving = ref(false);
const loadError = ref("");
const deleting = ref(null);
const form = reactive(emptyForm());

function emptyForm() {
  return { id: null, mobile: "", displayName: "", password: "", enabled: true };
}

function resetForm() {
  Object.assign(form, emptyForm());
}

function edit(item) {
  Object.assign(form, { ...item, password: "" });
  window.scrollTo({ top: 0, behavior: "smooth" });
}

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

async function save() {
  if (saving.value) return;
  saving.value = true;
  try {
    await api.saveStudent({ ...form });
    showNotice(form.id ? "学生账号修改成功" : "学生账号新增成功");
    resetForm();
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    saving.value = false;
  }
}

async function confirmDelete() {
  try {
    await api.deleteStudent(deleting.value.id);
    showNotice("学生账号已删除");
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
    <header class="page-title"><div><span>STUDENT ACCOUNTS</span><h1>学生账号管理</h1><p>维护学生登录账号，供其查看自己的订单与咨询记录。</p></div><button class="ghost" @click="resetForm">新增学生账号</button></header>
    <div class="admin-layout">
      <form class="card editor" @submit.prevent="save">
        <div class="editor-title"><div><span>{{ form.id ? "EDIT STUDENT" : "NEW STUDENT" }}</span><h2>{{ form.id ? "编辑学生账号" : "新增学生账号" }}</h2></div><button v-if="form.id" type="button" class="text-action" @click="resetForm">取消编辑</button></div>
        <label>手机号<input v-model="form.mobile" required /></label>
        <label>显示名称<input v-model="form.displayName" required /></label>
        <label>登录密码<input v-model="form.password" :placeholder="form.id ? '留空则不修改密码' : '请输入初始密码'" /></label>
        <label class="check"><input v-model="form.enabled" type="checkbox" /> 允许学生登录</label>
        <button class="primary" :disabled="saving">{{ saving ? "正在保存…" : form.id ? "保存修改" : "新增账号" }}</button>
      </form>
      <div class="list">
        <div v-if="loading" class="empty-state">学生账号加载中…</div>
        <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>
        <article v-for="item in items" v-else :key="item.id" class="card row journal-row">
          <div><b>{{ item.displayName }}</b><small>{{ item.mobile }} · {{ item.enabled ? "已启用" : "已停用" }}</small></div>
          <button @click="edit(item)">编辑</button><button class="danger" @click="deleting = item">删除</button>
        </article>
        <div v-if="!loading && !loadError && !items.length" class="empty-state"><b>暂无学生账号</b><span>使用左侧表单添加第一个学生登录账号。</span></div>
      </div>
    </div>
    <ConfirmDialog v-if="deleting" title="删除学生账号？" :message="`将删除“${deleting.displayName}”的登录账号。`" @cancel="deleting = null" @confirm="confirmDelete" />
  </section>
</template>
