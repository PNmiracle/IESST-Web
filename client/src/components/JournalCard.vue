<script setup>
import { computed, ref } from "vue";

const props = defineProps({
  journal: { type: Object, required: true },
});
const imageFailed = ref(false);

const submitLink = computed(() => ({
  path: "/submit",
  query: {
    subject: props.journal.title,
    target: props.journal.type || "SCI",
  },
}));

function display(value, fallback = "-") {
  return value === undefined || value === null || value === "" ? fallback : value;
}
</script>

<template>
  <article class="journal-showcase-card">
    <figure class="journal-cover-frame">
      <img
        v-if="journal.imageUrl && !imageFailed"
        :src="journal.imageUrl"
        :alt="journal.title"
        loading="lazy"
        decoding="async"
        @error="imageFailed = true"
      />
      <div v-else class="journal-cover-placeholder">
        <b>{{ display(journal.type) }}</b>
        <span>{{ display(journal.disciplineCategory || journal.field, "期刊方向") }}</span>
      </div>
    </figure>
    <div class="journal-card-body">
      <h3>{{ journal.title }}</h3>
      <dl class="journal-meta-grid">
        <div>
          <dt>期刊级别：</dt>
          <dd>{{ display(journal.journalLevel || journal.indexType) }}</dd>
        </div>
        <div>
          <dt>学科领域：</dt>
          <dd>{{ display(journal.disciplineCategory || journal.field) }}</dd>
        </div>
        <div>
          <dt>中科院分区：</dt>
          <dd>{{ display(journal.casZone) }}</dd>
        </div>
        <div>
          <dt>JCR分区：</dt>
          <dd>{{ display(journal.jcrQuartile) }}</dd>
        </div>
        <div>
          <dt>影响因子：</dt>
          <dd>{{ display(journal.impactFactorLabel) }}</dd>
        </div>
      </dl>
      <RouterLink class="journal-submit-button" :to="submitLink">立即投稿</RouterLink>
    </div>
  </article>
</template>
