<script setup>
import { computed, onMounted, ref } from "vue";
import { api } from "../../api";

const props = defineProps({ journalType: { type: String, required: true } });
const journals = ref([]);
const loading = ref(true);
const error = ref("");
const keyword = ref("");
const filtered = computed(() => journals.value.filter((item) => item.type === props.journalType && (!keyword.value.trim() || `${item.title} ${item.field} ${item.description}`.toLowerCase().includes(keyword.value.trim().toLowerCase()))));

onMounted(async () => {
  try { journals.value = await api.publicJournals(); }
  catch (exception) { error.value = exception.message; }
  finally { loading.value = false; }
});
</script>

<template>
  <section class="page-hero"><div class="shell"><span>{{ journalType }} JOURNAL CATALOG</span><h1>{{ journalType }} 期刊资源</h1><p>浏览独立的 {{ journalType }} 期刊目录，按研究方向快速筛选并查看详情。</p></div></section>
  <section class="section shell">
    <div class="journal-tools"><div><span class="tag">{{ journalType }} 专区</span></div><input v-model="keyword" type="search" :placeholder="`搜索 ${journalType} 期刊名称或研究方向`" /></div>
    <div v-if="loading" class="empty-state">期刊列表加载中…</div>
    <div v-else-if="error" class="empty-state error-state">{{ error }}</div>
    <div v-else-if="filtered.length" class="journal-grid">
      <article v-for="journal in filtered" :key="journal.id"><span class="tag">{{ journal.type }} · {{ journal.field }}</span><h3>{{ journal.title }}</h3><p>{{ journal.description }}</p><div><b>{{ journal.indexType }}</b><small>{{ journal.cycle }}</small></div><RouterLink :to="`/${journal.type}/${journal.id}`">查看期刊详情 →</RouterLink></article>
    </div>
    <div v-else class="empty-state">没有匹配的 {{ journalType }} 期刊，请调整关键词。</div>
  </section>
</template>
