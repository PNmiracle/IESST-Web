<script setup>
defineProps({
  text: {
    type: String,
    default: "",
  },
});

function commaLines(value) {
  return String(value).match(/[^，,]+(?:[，,]|$)/g)?.map((line) => line.trim()).filter(Boolean) || [];
}
</script>

<template>
  <span class="smart-comma-text">
    <span v-for="(line, index) in commaLines(text)" :key="`${index}-${line}`">{{ line }}</span>
  </span>
</template>
