<script setup lang="ts">
import type { Field } from '~/types/mentorship'

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
      <li v-for="field in fields" :key="field.slug">
        <!-- Each Field links into discovery, so a crawler reaches the Mentors too. -->
        <NuxtLink :to="{ path: '/mentors', query: { field: field.slug } }">
          {{ field.displayName }}
        </NuxtLink>
      </li>
    </ul>
  </main>
</template>
