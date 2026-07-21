<script setup lang="ts">
interface Status {
  service: string
  status: string
  time: string
}

// Fetched server-side during SSR via the BFF route — this is the end-to-end
// proof that the frontend reaches the backend and renders the result.
const { data: status, error } = await useFetch<Status>('/api/status')
</script>

<template>
  <main>
    <h1>Reinvent</h1>
    <p v-if="error">Backend unreachable: {{ error.message }}</p>
    <section v-else-if="status">
      <p>Service: {{ status.service }}</p>
      <p>Status: {{ status.status }}</p>
      <p>Time: {{ status.time }}</p>
    </section>
  </main>
</template>
