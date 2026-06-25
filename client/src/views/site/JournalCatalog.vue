<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { api } from "../../api";
import JournalCard from "../../components/JournalCard.vue";

const props = defineProps({ journalType: { type: String, required: true } });

const journals = ref([]);
const loading = ref(true);
const error = ref("");
const filters = reactive(emptyFilters());

const disciplineOptions = ["不限", "土木建筑工程", "交通物流工程", "环境科学工程", "机械电子工程", "计算机信息工程", "材料科学", "数学物理", "化学工程", "生物学", "医学"];

function emptyFilters() {
  return {
    discipline: "不限",
  };
}

function requestParams() {
  return {
    type: props.journalType,
    discipline: filters.discipline,
  };
}

async function load() {
  loading.value = true;
  error.value = "";
  try {
    journals.value = await api.publicJournals(requestParams());
  } catch (exception) {
    error.value = exception.message;
  } finally {
    loading.value = false;
  }
}

function setOption(key, value) {
  filters[key] = value;
  load();
}

function resetFilters() {
  Object.assign(filters, emptyFilters());
  load();
}

onMounted(load);
watch(() => props.journalType, resetFilters);
</script>

<template>
  <section class="page-hero">
    <div class="shell">
      <span>{{ journalType }} JOURNAL CATALOG</span>
      <h1>{{ journalType }} 期刊资源</h1>
      <p>按学科领域快速筛选适合的投稿方向。</p>
    </div>
  </section>

  <section class="section shell journal-catalog-page">
    <nav class="catalog-breadcrumb" aria-label="当前位置">
      <RouterLink to="/">首页</RouterLink>
      <span>&gt;</span>
      <b>{{ journalType }}期刊</b>
    </nav>

    <div class="journal-filter-panel">
      <div class="filter-row">
        <b>学科领域：</b>
        <button v-for="item in disciplineOptions" :key="item" :class="{ active: filters.discipline === item }" @click="setOption('discipline', item)">{{ item }}</button>
      </div>
    </div>

    <div class="journal-sort-bar">
      <small>共 <strong>{{ journals.length }}</strong> 条</small>
    </div>

    <div v-if="loading" class="empty-state">期刊列表加载中…</div>
    <div v-else-if="error" class="empty-state error-state">{{ error }}<button class="ghost" @click="load">重新加载</button></div>
    <div v-else-if="journals.length" class="journal-showcase-grid catalog-results-grid">
      <JournalCard v-for="journal in journals" :key="journal.id" :journal="journal" />
    </div>
    <div v-else class="empty-state">没有匹配的 {{ journalType }} 期刊，请调整筛选条件。</div>
  </section>
</template>
