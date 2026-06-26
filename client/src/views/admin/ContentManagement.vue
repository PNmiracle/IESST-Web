<script setup>
import { onMounted, reactive, ref } from "vue";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const tab = ref("services");
const services = ref([]);
const experts = ref([]);
const serviceForm = reactive(emptyService());
const expertForm = reactive(emptyExpert());
function emptyService(){return {id:null,category:"translation",title:"",price:"",description:"",features:"",published:true};}
function emptyExpert(){return {id:null,name:"",institution:"",role:"学术编辑 · 编辑专家",imageUrl:"/images/experts/sos-agaian.jpg",published:true};}
function resetService(){Object.assign(serviceForm,emptyService());}
function resetExpert(){Object.assign(expertForm,emptyExpert());}
async function load(){[services.value,experts.value]=await Promise.all([api.adminServices(),api.adminExperts()]);}
async function saveService(){await api.saveService({...serviceForm});showNotice("服务内容已保存");resetService();await load();}
async function saveExpert(){await api.saveExpert({...expertForm});showNotice("专家信息已保存");resetExpert();await load();}
async function removeService(id){if(confirm("确认删除该服务？")){await api.deleteService(id);await load();}}
async function removeExpert(id){if(confirm("确认删除该专家？")){await api.deleteExpert(id);await load();}}
onMounted(load);
</script>

<template>
  <section class="admin-page">
    <header class="page-title"><div><span>SITE CONTENT</span><h1>官网内容管理</h1><p>维护前端 v2 中的翻译润色、科学编辑服务和专家团队。</p></div><div class="segmented"><button :class="{active:tab==='services'}" @click="tab='services'">服务套餐</button><button :class="{active:tab==='experts'}" @click="tab='experts'">专家团队</button></div></header>
    <div v-if="tab==='services'" class="admin-layout">
      <form class="card editor" @submit.prevent="saveService"><div class="editor-title"><div><span>SERVICE EDITOR</span><h2>{{serviceForm.id?'编辑服务':'新增服务'}}</h2></div><button type="button" class="text-action" @click="resetService">重置</button></div><label>所属栏目<select v-model="serviceForm.category"><option value="translation">翻译润色</option><option value="editing">科学编辑</option></select></label><label>服务名称<input v-model="serviceForm.title" required /></label><label>价格说明<input v-model="serviceForm.price" /></label><label>服务简介<textarea v-model="serviceForm.description"></textarea></label><label>服务特点（每行一项）<textarea v-model="serviceForm.features"></textarea></label><label class="check"><input v-model="serviceForm.published" type="checkbox" /> 官网展示</label><button class="primary">保存服务</button></form>
      <div class="list"><article v-for="item in services" :key="item.id" class="card row journal-row"><div><span class="tag">{{item.category==='translation'?'翻译润色':'科学编辑'}}</span><b>{{item.title}}</b><small>{{item.price}} · {{item.published?'已展示':'未展示'}}</small></div><button @click="Object.assign(serviceForm,item)">编辑</button><button class="danger" @click="removeService(item.id)">删除</button></article></div>
    </div>
    <div v-else class="admin-layout">
      <form class="card editor" @submit.prevent="saveExpert"><div class="editor-title"><div><span>EXPERT EDITOR</span><h2>{{expertForm.id?'编辑专家':'新增专家'}}</h2></div><button type="button" class="text-action" @click="resetExpert">重置</button></div><label>姓名<input v-model="expertForm.name" required /></label><label>机构<input v-model="expertForm.institution" /></label><label>角色<input v-model="expertForm.role" /></label><label>照片地址<input v-model="expertForm.imageUrl" /></label><label class="check"><input v-model="expertForm.published" type="checkbox" /> 官网展示</label><button class="primary">保存专家</button></form>
      <div class="list"><article v-for="item in experts" :key="item.id" class="card row"><img :src="item.imageUrl" :alt="item.name" /><div><b>{{item.name}}</b><small>{{item.institution}} · {{item.published?'已展示':'未展示'}}</small></div><button @click="Object.assign(expertForm,item)">编辑</button><button class="danger" @click="removeExpert(item.id)">删除</button></article></div>
    </div>
  </section>
</template>
