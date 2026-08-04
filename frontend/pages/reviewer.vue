<script setup lang="ts">
import type { MentorApplication } from '~/types/mentorship'

const request = useBackendFetch()

// The backend gates both reads to the REVIEWER role (403 for anyone else); surface
// that rather than erroring the page.
const forbidden = ref(false)
const onForbidden = (e: { statusCode?: number }) => {
  forbidden.value = forbidden.value || e?.statusCode === 403
  return []
}

const { data: queue, refresh: refreshQueue } = await useAsyncData<MentorApplication[]>('reviewer-queue', () =>
  request('/api/reviewer/applications').catch(onForbidden) as Promise<MentorApplication[]>,
)
const { data: mentors, refresh: refreshMentors } = await useAsyncData<MentorApplication[]>('reviewer-mentors', () =>
  request('/api/reviewer/applications/mentors').catch(onForbidden) as Promise<MentorApplication[]>,
)

async function refresh() {
  await Promise.all([refreshQueue(), refreshMentors()])
}

async function act(id: string, action: 'start-review' | 'approve' | 'suspend' | 'reinstate') {
  await $fetch(`/api/reviewer/applications/${id}/${action}`, { method: 'POST' })
  await refresh()
}

async function reject(id: string) {
  const reason = window.prompt('Reason for rejection?')
  if (!reason) return
  await $fetch(`/api/reviewer/applications/${id}/reject`, { method: 'POST', body: { reason } })
  await refresh()
}
</script>

<template>
  <main>
    <h1>Reviewer</h1>

    <p v-if="forbidden">This area is for Reviewers only.</p>

    <template v-else>
      <section>
        <h2>Applications awaiting review</h2>
        <p v-if="!queue || queue.length === 0">No applications awaiting review.</p>
        <ul v-else>
          <li v-for="app in queue" :key="app.id">
            <span>{{ app.applicantUserId }} — {{ app.status }}</span>
            <button v-if="app.status === 'APPLIED'" @click="act(app.id, 'start-review')">Start review</button>
            <template v-if="app.status === 'UNDER_REVIEW'">
              <button @click="act(app.id, 'approve')">Approve</button>
              <button @click="reject(app.id)">Reject</button>
            </template>
          </li>
        </ul>
      </section>

      <section>
        <h2>Approved Mentors</h2>
        <p v-if="!mentors || mentors.length === 0">No approved Mentors.</p>
        <ul v-else>
          <li v-for="mentor in mentors" :key="mentor.id">
            <span>{{ mentor.applicantUserId }} — {{ mentor.suspended ? 'suspended' : 'active' }}</span>
            <button v-if="!mentor.suspended" @click="act(mentor.id, 'suspend')">Suspend</button>
            <button v-else @click="act(mentor.id, 'reinstate')">Reinstate</button>
          </li>
        </ul>
      </section>
    </template>
  </main>
</template>
