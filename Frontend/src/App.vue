<script setup>
import { RouterView } from 'vue-router';
import ToastContainer from '@/components/ToastContainer.vue';
</script>

<template>
  <RouterView />
  <ToastContainer />
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Inter', sans-serif;
}
</style>
