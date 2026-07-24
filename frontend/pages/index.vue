<script setup lang="ts">
interface Status {
  service: string
  status: string
  time: string
}

// Fetched server-side during SSR via the BFF route — proof the frontend reaches
// the backend.
const { data: status } = await useFetch<Status>('/api/status')
</script>

<template>
  <main>
    <h1>Reinvent</h1>
    <p>A mentorship marketplace for teenagers exploring a career path.</p>
    <p v-if="status">
      Backend {{ status.service }} — {{ status.status }} @ {{ status.time }}
    </p>
    <p>
      <NuxtLink to="/fields">Explore career Fields</NuxtLink>
    </p>
  </main>
</template>
