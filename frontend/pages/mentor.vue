<script setup lang="ts">
interface MentorApplication {
  id: string
  status: 'APPLIED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED'
  rejectionReason: string | null
  suspended: boolean
  bookable: boolean
}

const request = useBackendFetch()

// GET /me is 404 when the User has never applied; treat that as "no application".
const { data: application, refresh } = await useAsyncData<MentorApplication | null>('my-application', () =>
  request('/api/mentor-applications/me').catch(() => null) as Promise<MentorApplication | null>,
)

const error = ref<string | null>(null)

async function apply() {
  error.value = null
  try {
    await $fetch('/api/mentor-applications', { method: 'POST' })
    await refresh()
  } catch {
    error.value = 'Could not submit your application.'
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

    <p v-if="error">{{ error }}</p>
  </main>
</template>
