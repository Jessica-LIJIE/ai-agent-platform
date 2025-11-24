<template>
  <div class="home-container">
    <h1>{{ message || 'Loading...' }}</h1>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHello } from '@/api'

const message = ref<string>('')

const fetchHello = async () => {
  try {
    message.value = await getHello()
  } catch (error) {
    console.error('Failed to fetch hello:', error)
    message.value = 'Error loading data'
  }
}

onMounted(() => {
  fetchHello()
})
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

h1 {
  font-size: 2rem;
  color: #333;
}
</style>

