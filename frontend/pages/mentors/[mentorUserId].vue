<script setup lang="ts">
import type { Field, PublicMentorProfile } from '~/types/mentorship'

// A Mentor's public profile at a shareable, server-rendered URL, reached only through the
// BFF. Only discoverable Mentors have one, so the backend answers 404 for an unfinished
// or suspended profile and this page shows that as "not available" rather than a blank.
const route = useRoute()
const mentorUserId = computed(() => String(route.params.mentorUserId))

const { data: profile } = await useFetch<PublicMentorProfile>(
  () => `/api/mentors/${mentorUserId.value}`,
)
const { data: fields } = await useFetch<Field[]>('/api/fields')

// Answer with a real 404 rather than a 200 carrying an apology, so a search engine
// drops a profile that is no longer public instead of indexing a dead page.
const event = useRequestEvent()
if (!profile.value && event) {
  setResponseStatus(event, 404)
}

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
      <!-- Deliberately says nothing about why: an unknown Mentor, an unfinished
           profile and a suspended one must be indistinguishable from out here. -->
      <p>This profile isn't public right now. Try browsing for another Mentor.</p>
      <p>
        <NuxtLink to="/mentors">Find a Mentor</NuxtLink>
      </p>
    </template>
  </main>
</template>
