<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";

const route = useRoute();
const items = ref([]);
const meta = {
  translation: { eyebrow:"LANGUAGE SERVICES",title:"翻译润色",subtitle:"让研究成果以准确、自然、符合国际期刊规范的语言呈现。",image:"/images/translation-polish.jpg",highlights:["9大学科领域编辑团队","学科专家与语言编辑双重审核","严格保护作者原意与研究内容"] },
  editing: { eyebrow:"SCIENTIFIC EDITING",title:"科学编辑",subtitle:"从稿件诊断到返修回复，帮助作者清晰呈现研究价值。",image:"/images/scientific-editing.jpg",highlights:["编辑一对一沟通","忠于研究原意与学术伦理","按稿件阶段定制服务方案"] },
};
const service = computed(() => ({ ...(meta[route.params.kind] || meta.translation), items:items.value.filter(item => item.category === route.params.kind) }));
function consultHref(subject, targetType) {
  const query = new URLSearchParams({ consult: "1", subject, targetType });
  return `${route.path}?${query.toString()}`;
}
onMounted(async () => { items.value = await api.publicServices(); });
</script>

<template>
  <section class="page-hero service-page-hero">
    <div class="shell">
      <span>{{ service.eyebrow }}</span>
      <h1>{{ service.title }}</h1>
      <p>{{ service.subtitle }}</p>
      <a class="primary" :href="consultHref(service.title, service.title)" :data-consult-subject="service.title" :data-consult-target="service.title" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">获取服务方案</a>
    </div>
  </section>
  <section class="section shell service-list-section">
    <div class="journal-tools service-tools">
      <div><span class="tag">{{ service.title }}专区</span></div>
      <div class="service-highlight-inline">
        <span v-for="item in service.highlights" :key="item">✓ {{ item }}</span>
      </div>
    </div>
    <div class="heading">
      <div><span>SERVICE TYPES</span><h2>服务类型</h2></div>
      <p>根据稿件阶段和实际需求选择服务，提交后由顾问进一步确认范围。</p>
    </div>
    <div class="service-card-grid"><article v-for="item in service.items" :key="item.id" class="card service-card"><span>{{ item.price }}</span><h3>{{ item.title }}</h3><p>{{ item.description }}</p><ul><li v-for="feature in item.features.split('\n').filter(Boolean)" :key="feature">{{ feature }}</li></ul><a class="primary" :href="consultHref(`${service.title}-${item.title}`, service.title)" :data-consult-subject="`${service.title}-${item.title}`" :data-consult-target="service.title" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">立即咨询</a></article></div>
  </section>
  <section class="section shell service-material-section"><div class="heading"><div><span>SERVICE OVERVIEW</span><h2>{{ service.title }}服务概览</h2></div><p>将参考素材拆成独立内容区，图片负责展示版式氛围，页面文字与业务信息由组件单独承载。</p></div><figure class="card service-material-card"><img :src="service.image" :alt="`${service.title}服务概览`" /></figure></section>
</template>
