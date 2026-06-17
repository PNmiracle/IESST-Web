<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";
const route = useRoute();
const form = reactive({ authorName:"", email:"", paperTitle:"", targetType:"SCI", message:"" });
const submitting = ref(false);
const result = ref(null);
onMounted(() => { if (route.query.target) form.targetType = route.query.target; if (route.query.subject) form.message = `咨询来源：${route.query.subject}`; });
async function submit() { if (submitting.value) return; submitting.value=true; try { result.value=await api.submit({...form,id:null,status:null,createdAt:null}); showNotice("信息已成功提交"); } catch(e){ showNotice(e.message,true); } finally{submitting.value=false;} }
</script>

<template>
  <section class="page-hero"><div class="shell"><span>ONLINE ASSESSMENT</span><h1>免费评估稿件</h1><p>提交基础信息后，管理员后台会立即收到记录并跟进处理。</p></div></section>
  <section class="section shell submit-page-grid"><div><h2>提交前可以准备</h2><ul><li>论文标题与研究方向</li><li>目标期刊类型或服务需求</li><li>当前稿件阶段和希望解决的问题</li></ul><div class="card privacy-note"><b>隐私说明</b><p>提交信息会保存到 MySQL 数据库，仅供管理员在后台跟进服务咨询。请勿上传或填写尚未公开的敏感实验数据。</p></div></div><section v-if="result" class="card submit-success"><span>✓</span><h3>信息已收到</h3><p>记录编号：#{{ result.id }}，当前状态：{{ result.status }}</p><button class="ghost" @click="result=null">继续提交</button></section><form v-else class="card form-grid" @submit.prevent="submit"><label>作者姓名<input v-model="form.authorName" required /></label><label>联系邮箱<input v-model="form.email" type="email" required /></label><label class="wide">论文标题<input v-model="form.paperTitle" required /></label><label>目标类型<select v-model="form.targetType"><option>SCI</option><option>EI</option><option>翻译润色</option><option>科学编辑</option></select></label><label>补充说明<textarea v-model="form.message"></textarea></label><button class="primary wide" :disabled="submitting">{{ submitting ? "正在提交…" : "提交评估信息" }}</button></form></section>
</template>
