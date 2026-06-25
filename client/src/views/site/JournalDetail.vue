<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../../api";
const route = useRoute();
const props = defineProps({ journalType: { type: String, required: true } });
const journal = ref(null);
const error = ref("");
function consultHref(subject, targetType) {
  const query = new URLSearchParams({ consult: "1", subject, targetType });
  return `${route.path}?${query.toString()}`;
}
function display(value, fallback = "-") {
  return value === undefined || value === null || value === "" ? fallback : value;
}
onMounted(async () => { try { const item = await api.publicJournal(route.params.id); if (item.type !== props.journalType) throw new Error("期刊栏目不匹配"); journal.value = item; } catch (exception) { error.value = exception.message; } });
</script>

<template>
  <div v-if="error" class="section shell empty-state error-state">{{ error }}<RouterLink class="ghost" :to="`/${journalType}`">返回期刊列表</RouterLink></div>
  <template v-else-if="journal">
    <section class="detail-hero"><div class="shell detail-hero-grid"><figure v-if="journal.imageUrl" class="journal-detail-cover"><img :src="journal.imageUrl" :alt="journal.title" /></figure><div><span class="tag">{{ journal.type }} · {{ journal.disciplineCategory || journal.field }}</span><h1>{{ journal.title }}</h1><p>{{ journal.description }}</p><div class="detail-actions"><a class="primary" :href="consultHref(journal.title, journal.type)" :data-consult-subject="journal.title" :data-consult-target="journal.type" onclick="window.__iesstConsultationFromElement && window.__iesstConsultationFromElement(this)">申请适配评估</a><a v-if="journal.documentUrl" class="ghost" :href="journal.documentUrl" target="_blank" rel="noreferrer">下载期刊文档</a><RouterLink class="ghost" :to="`/${journal.type}`">返回列表</RouterLink></div></div><aside class="card metric-panel"><div><span>期刊级别</span><b>{{ display(journal.journalLevel || journal.indexType) }}</b></div><div><span>影响因子</span><b>{{ display(journal.impactFactorLabel) }}</b></div><div><span>期刊分区</span><b>{{ display(journal.journalPartition) }}</b></div><div><span>录用时间</span><b>{{ display(journal.acceptanceTime || journal.cycle) }}</b></div><div><span>截稿时间</span><b>{{ display(journal.submissionDeadlineText) }}</b></div></aside></div></section>
    <section class="section shell detail-content"><article><span class="eyebrow">AIMS & SCOPE</span><h2>期刊方向与稿件建议</h2><p>{{ journal.description }} 建议作者在投稿前明确研究问题、方法创新点与实际贡献，并保证摘要、图表和结论能够形成一致的研究叙事。</p></article><article><span class="eyebrow">SUPPORT FLOW</span><h2>适配评估流程</h2><ol><li>提交标题、摘要与研究方向</li><li>评估期刊范围与稿件阶段</li><li>提供周期、编辑与投稿建议</li><li>按需求进入后续服务</li></ol></article></section>
  </template>
  <div v-else class="section shell empty-state">期刊详情加载中…</div>
</template>
