<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { api } from "../../api";
import JournalCard from "../../components/JournalCard.vue";

const props = defineProps({ journalType: { type: String, required: true } });

const journals = ref([]);
const loading = ref(true);
const error = ref("");
const filters = reactive(emptyFilters());
const currentPage = ref(1);
const pageSize = 8;
const totalPages = computed(() => Math.max(1, Math.ceil(journals.value.length / pageSize)));
const pagedJournals = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return journals.value.slice(start, start + pageSize);
});
const pageNumbers = computed(() => Array.from({ length: totalPages.value }, (_, index) => index + 1));

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
  currentPage.value = 1;
  load();
}

function resetFilters() {
  Object.assign(filters, emptyFilters());
  currentPage.value = 1;
  load();
}

function changePage(page) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return;
  currentPage.value = page;
  document.querySelector(".journal-catalog-page")?.scrollIntoView({ behavior: "smooth", block: "start" });
}

onMounted(load);
watch(() => props.journalType, resetFilters);
</script>

<template>
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
      <small>共 <strong>{{ journals.length }}</strong> 条，每页 8 条</small>
    </div>

    <div v-if="loading" class="empty-state">期刊列表加载中…</div>
    <div v-else-if="error" class="empty-state error-state">{{ error }}<button class="ghost" @click="load">重新加载</button></div>
    <div v-else-if="journals.length" class="journal-showcase-grid catalog-results-grid">
      <JournalCard v-for="journal in pagedJournals" :key="journal.id" :journal="journal" />
    </div>
    <div v-else class="empty-state">没有匹配的 {{ journalType }} 期刊，请调整筛选条件。</div>

    <nav v-if="!loading && !error && journals.length && totalPages > 1" class="catalog-pagination" aria-label="期刊列表分页">
      <button type="button" :disabled="currentPage === 1" aria-label="上一页" @click="changePage(currentPage - 1)">‹</button>
      <button
        v-for="page in pageNumbers"
        :key="page"
        type="button"
        :class="{ active: currentPage === page }"
        :aria-current="currentPage === page ? 'page' : undefined"
        :aria-label="`第 ${page} 页`"
        @click="changePage(page)"
      >{{ page }}</button>
      <button type="button" :disabled="currentPage === totalPages" aria-label="下一页" @click="changePage(currentPage + 1)">›</button>
    </nav>
  </section>
</template>
