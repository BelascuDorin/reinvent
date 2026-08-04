<script setup lang="ts">
import type { Field, MentorSummary } from '~/types/mentorship'

// Public, server-rendered discovery (no account needed), reaching the backend only
// through the BFF. The search lives in the URL rather than in component state, so a
// result page is shareable and indexable — which is why this page is SSR at all.
const route = useRoute()

const asFilter = (value: unknown) => (typeof value === 'string' && value !== '' ? value : '')
const field = computed(() => asFilter(route.query.field))
const language = computed(() => asFilter(route.query.language))

const { data: fields } = await useFetch<Field[]>('/api/fields')
const { data: mentors } = await useFetch<MentorSummary[]>('/api/mentors', {
  query: computed(() => ({ field: field.value || undefined, language: language.value || undefined })),
})

// The form edits a draft of the search; submitting writes it to the URL, which is what
// actually drives the fetch above.
const fieldChoice = ref(field.value)
const languageChoice = ref(language.value)
watch([field, language], ([nextField, nextLanguage]) => {
  fieldChoice.value = nextField
  languageChoice.value = nextLanguage
})

function search() {
  return navigateTo({
    query: {
      ...(fieldChoice.value ? { field: fieldChoice.value } : {}),
      ...(languageChoice.value ? { language: languageChoice.value } : {}),
    },
  })
}

const fieldName = (slug: string) => fields.value?.find((f) => f.slug === slug)?.displayName ?? slug

// Every listed Mentor is discoverable, which means a price and a Meeting duration are
// set — so there is no missing-price case to render here.
const price = (mentor: MentorSummary) => `${mentor.priceAmount} ${mentor.priceCurrency}`
</script>

<template>
  <main>
    <h1>Find a Mentor</h1>
    <p>Browse Mentors by the career Field you want to get into.</p>

    <form @submit.prevent="search">
      <p>
        <label>
          Field
          <select v-model="fieldChoice">
            <option value="">All Fields</option>
            <option v-for="option in fields" :key="option.slug" :value="option.slug">
              {{ option.displayName }}
            </option>
          </select>
        </label>
      </p>
      <p>
        <label>Language <input v-model="languageChoice" type="text" placeholder="e.g. en" /></label>
      </p>
      <button type="submit">Search</button>
    </form>

    <p v-if="!mentors || mentors.length === 0">
      No Mentors are available here yet. Try another Field or clear the language filter.
    </p>
    <ul v-else>
      <li v-for="mentor in mentors" :key="mentor.mentorUserId">
        <h2>{{ mentor.displayName ?? 'A Mentor' }}</h2>
        <p v-if="mentor.roleTitle">{{ mentor.roleTitle }}</p>
        <p>{{ price(mentor) }} · {{ mentor.meetingDurationMinutes }} minute Meeting</p>
        <p>Fields: {{ mentor.fieldSlugs.map(fieldName).join(', ') }}</p>
        <p v-if="mentor.languages.length">Speaks: {{ mentor.languages.join(', ') }}</p>
      </li>
    </ul>
  </main>
</template>
