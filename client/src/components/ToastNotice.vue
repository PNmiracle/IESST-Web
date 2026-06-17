<script setup>
import { notice } from "../stores/notice";
</script>

<template>
  <div v-if="notice.message" :class="['toast', { error: notice.error }]">{{ notice.message }}</div>
</template>
