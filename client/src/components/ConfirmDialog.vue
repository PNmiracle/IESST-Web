<script setup>
defineProps({ title: String, message: String });
defineEmits(["confirm", "cancel"]);
</script>

<template>
  <div class="dialog-backdrop" @click.self="$emit('cancel')">
    <section class="dialog card">
      <span class="dialog-icon">!</span>
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      <div><button class="ghost" @click="$emit('cancel')">取消</button><button class="danger-solid" @click="$emit('confirm')">确认删除</button></div>
    </section>
  </div>
</template>
