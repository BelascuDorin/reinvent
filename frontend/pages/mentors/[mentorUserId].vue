<script setup lang="ts">
import type { Field, PublicMentorProfile } from '~/types/mentorship'

// A Mentor's public profile at a shareable, server-rendered URL, reached only through the
// BFF. Only discoverable Mentors have one, so the backend answers 404 for an unfinished
// or suspended profile and this page shows that as "not available" rather than a blank.
const route = useRoute()
const mentorUserId = computed(() => String(route.params.mentorUserId))

const { data: profile, error } = await useFetch<PublicMentorProfile>(
  () => `/api/mentors/${mentorUserId.value}`,
)
const { data: fields } = await useFetch<Field[]>('/api/fields')

useHead(() => ({
  title: profile.value?.displayName
    ? `${profile.value.displayName} — Mentor on Reinvent`
    : 'Mentor — Reinvent',
}))
</script>

<template>
  <main>
    <template v-if="profile">
      <MentorPublicProfile :profile="profile" :fields="fields" />
    </template>
    <template v-else>
      <h1>This Mentor isn't available</h1>
      <p v-if="error">
        They may have paused their profile. Try browsing for another Mentor.
      </p>
      <p>
        <NuxtLink to="/mentors">Find a Mentor</NuxtLink>
      </p>
    </template>
  </main>
</template>
