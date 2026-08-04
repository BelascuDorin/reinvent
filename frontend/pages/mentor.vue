<script setup lang="ts">
import type { Field, MentorProfile } from '~/types/mentorship'

interface MentorApplication {
  id: string
  status: 'APPLIED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED'
  rejectionReason: string | null
  suspended: boolean
  bookable: boolean
}

interface ProfileForm {
  displayName: string
  roleTitle: string
  bio: string
  experience: string
  employer: string
  priceAmount: string
  priceCurrency: string
  meetingDurationMinutes: string
  fieldSlugs: string[]
  languages: string
}

const request = useBackendFetch()

// GET /me is 404 when the User has never applied; treat that as "no application".
const { data: application, refresh } = await useAsyncData<MentorApplication | null>('my-application', () =>
  request('/api/mentor-applications/me').catch(() => null) as Promise<MentorApplication | null>,
)

// The profile only exists once approved (draft provisioned on approval); before then
// the endpoint is 403/404, which we treat as "no profile yet".
const { data: profile, refresh: refreshProfile } = await useAsyncData<MentorProfile | null>('my-profile', () =>
  request('/api/mentor-profiles/me').catch(() => null) as Promise<MentorProfile | null>,
)

const { data: fields } = await useAsyncData<Field[]>('fields', () =>
  request('/api/fields').catch(() => []) as Promise<Field[]>,
)

const error = ref<string | null>(null)
const saved = ref(false)

function toForm(p: MentorProfile | null): ProfileForm {
  return {
    displayName: p?.displayName ?? '',
    roleTitle: p?.roleTitle ?? '',
    bio: p?.bio ?? '',
    experience: p?.experience ?? '',
    employer: p?.employer ?? '',
    priceAmount: p?.priceAmount != null ? String(p.priceAmount) : '',
    priceCurrency: p?.priceCurrency ?? 'USD',
    meetingDurationMinutes: p?.meetingDurationMinutes != null ? String(p.meetingDurationMinutes) : '',
    fieldSlugs: [...(p?.fieldSlugs ?? [])],
    languages: (p?.languages ?? []).join(', '),
  }
}

const form = ref<ProfileForm>(toForm(profile.value ?? null))

// What's still missing before this Mentor appears in discovery. The server's `complete`
// flag is the authority on whether anything is missing at all — this only names the gaps,
// so a rule the two disagree on can never show "complete" over an incomplete profile.
const missing = computed(() => {
  const p = profile.value
  if (!p || p.complete) return []
  const gaps: string[] = []
  if (p.fieldSlugs.length === 0) gaps.push('at least one Field')
  if (p.priceAmount == null || !p.priceCurrency) gaps.push('a price')
  if (p.meetingDurationMinutes == null) gaps.push('a Meeting duration')
  return gaps.length ? gaps : ['a few more details']
})

async function apply() {
  error.value = null
  try {
    await $fetch('/api/mentor-applications', { method: 'POST' })
    await refresh()
  } catch {
    error.value = 'Could not submit your application.'
  }
}

async function save() {
  error.value = null
  saved.value = false
  const body = {
    displayName: form.value.displayName,
    roleTitle: form.value.roleTitle,
    bio: form.value.bio,
    experience: form.value.experience,
    employer: form.value.employer,
    priceAmount: form.value.priceAmount ? Number(form.value.priceAmount) : null,
    priceCurrency: form.value.priceCurrency,
    meetingDurationMinutes: form.value.meetingDurationMinutes
      ? Number(form.value.meetingDurationMinutes)
      : null,
    fieldSlugs: form.value.fieldSlugs,
    languages: form.value.languages
      .split(',')
      .map((l) => l.trim())
      .filter(Boolean),
  }
  try {
    await $fetch('/api/mentor-profiles/me', { method: 'PUT', body })
    await refreshProfile()
    form.value = toForm(profile.value ?? null)
    saved.value = true
  } catch {
    error.value = 'Could not save your profile. Please check the fields and try again.'
  }
}
</script>

<template>
  <main>
    <h1>Become a Mentor</h1>

    <template v-if="application">
      <p>Application status: {{ application.status }}</p>
      <p v-if="application.status === 'REJECTED'">Reason: {{ application.rejectionReason }}</p>
      <p v-else-if="application.status === 'APPROVED' && application.suspended">
        You're approved, but your Mentor profile is currently suspended, so you can't be booked.
      </p>
      <p v-else-if="application.status === 'APPROVED' && !application.bookable">
        You're approved — but you can't be booked yet until you complete payment onboarding.
      </p>
      <button v-if="application.status === 'REJECTED'" @click="apply">Apply again</button>
    </template>

    <template v-else>
      <p>You haven't applied to be a Mentor yet.</p>
      <button @click="apply">Apply to be a Mentor</button>
    </template>

    <section v-if="profile">
      <h2>Your Mentor profile</h2>

      <p v-if="profile.discoverable">
        Your profile is complete and discoverable — Mentees can find you.
      </p>
      <p v-else-if="application?.suspended">
        Your profile is hidden from discovery while you're suspended. Your data is kept.
      </p>
      <p v-else-if="missing.length">
        To appear in discovery you still need: {{ missing.join(', ') }}.
      </p>
      <p v-else>Your profile is complete — it will appear in discovery.</p>

      <form @submit.prevent="save">
        <p>
          <label>Display name <input v-model="form.displayName" type="text" /></label>
        </p>
        <p>
          <label>Role / title <input v-model="form.roleTitle" type="text" /></label>
        </p>
        <p>
          <label>Bio <textarea v-model="form.bio" /></label>
        </p>
        <p>
          <label>Experience <textarea v-model="form.experience" /></label>
        </p>
        <p>
          <label>Employer (optional) <input v-model="form.employer" type="text" /></label>
        </p>
        <p>
          <label>Price <input v-model="form.priceAmount" type="number" min="0" step="0.01" /></label>
          <label>Currency <input v-model="form.priceCurrency" type="text" /></label>
        </p>
        <p>
          <label>Meeting duration (minutes)
            <input v-model="form.meetingDurationMinutes" type="number" min="1" /></label>
        </p>
        <p>
          <label>Languages (comma-separated) <input v-model="form.languages" type="text" /></label>
        </p>

        <fieldset>
          <legend>Field(s)</legend>
          <label v-for="field in fields" :key="field.slug">
            <input v-model="form.fieldSlugs" type="checkbox" :value="field.slug" />
            {{ field.displayName }}
          </label>
        </fieldset>

        <button type="submit">Save profile</button>
      </form>

      <p v-if="saved">Profile saved.</p>
    </section>

    <p v-if="error">{{ error }}</p>
  </main>
</template>
