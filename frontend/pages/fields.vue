<script setup lang="ts">
interface Field {
  slug: string
  displayName: string
}

// The curated career areas a visitor can explore — a public, server-rendered read
// through the BFF (no account needed), so the list is shareable and indexable.
const { data: fields } = await useFetch<Field[]>('/api/fields')
</script>

<template>
  <main>
    <h1>Explore career Fields</h1>
    <p>The career areas you can find a Mentor in on Reinvent.</p>
    <p v-if="!fields || fields.length === 0">No Fields are available yet.</p>
    <ul v-else>
      <li v-for="field in fields" :key="field.slug">{{ field.displayName }}</li>
    </ul>
  </main>
</template>
