<script setup lang="ts">
import type { Field, PublicMentorProfile } from '~/types/mentorship'

// One rendering of a Mentor's public presentation, used by both the public profile page
// and the Mentor's own preview — so "what Mentees will see" is not an approximation of
// the real page, it is the real page.
const { profile, fields } = defineProps<{
  profile: PublicMentorProfile
  fields?: Field[] | null
}>()

const fieldName = (slug: string) => fields?.find((field) => field.slug === slug)?.displayName ?? slug
</script>

<template>
  <article>
    <h1>{{ profile.displayName ?? 'A Mentor' }}</h1>
    <!-- Employer stands on its own: a Mentor may set it without a role/title, and
         completeness doesn't require either, so neither may hide the other. -->
    <p v-if="profile.roleTitle">{{ profile.roleTitle }}</p>
    <p v-if="profile.employer">{{ profile.employer }}</p>

    <p>{{ profile.priceAmount }} {{ profile.priceCurrency }} · {{ profile.meetingDurationMinutes }} minute Meeting</p>

    <p>Fields: {{ profile.fieldSlugs.map(fieldName).join(', ') }}</p>
    <p v-if="profile.languages.length">Speaks: {{ profile.languages.join(', ') }}</p>

    <section v-if="profile.bio">
      <h2>About</h2>
      <p>{{ profile.bio }}</p>
    </section>

    <section v-if="profile.experience">
      <h2>Experience</h2>
      <p>{{ profile.experience }}</p>
    </section>

    <section>
      <h2>Reviews</h2>
      <!-- The rating-summary slot. Empty until Mentees can leave reviews. -->
      <p v-if="profile.ratingSummary">
        Rated {{ profile.ratingSummary.averageRating }} from
        {{ profile.ratingSummary.reviewCount }} reviews
      </p>
      <p v-else>No reviews yet.</p>
    </section>
  </article>
</template>
