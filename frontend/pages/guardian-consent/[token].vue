<script setup lang="ts">
interface ConsentContext {
  menteeEmail: string
  guardianName: string
  purpose: string
  state: 'PENDING' | 'CONSENTED' | 'EXPIRED'
}

const route = useRoute()
const token = route.params.token as string

// The Guardian is not a User and has no session; this is a public page keyed only
// by the link token. Resolve its context server-side. $fetch is narrowed to a
// plain (url) => Promise<unknown>: the full typed-$fetch signature scores every
// Nitro route and blows TS's instantiation-depth limit (the BFF contract is the
// backend's, which Nuxt can't type anyway).
const backendFetch = $fetch as (url: string) => Promise<unknown>
const { data: context, error: loadError } = await useAsyncData<ConsentContext>(`consent-${token}`, () =>
  backendFetch(`/api/consent/${token}`) as Promise<ConsentContext>,
)

const confirmGuardian = ref(false)
const agreeToTerms = ref(false)
const submitted = ref(false)
const submitting = ref(false)
const submitError = ref<string | null>(null)

async function submit() {
  if (submitting.value) return // the link is single-use; don't let a double-click hit a 409
  submitting.value = true
  submitError.value = null
  try {
    await $fetch(`/api/consent/${token}`, {
      method: 'POST',
      body: { confirmGuardian: confirmGuardian.value, agreeToTerms: agreeToTerms.value },
    })
    submitted.value = true
  } catch {
    submitError.value = 'We could not record your consent. The link may have expired or already been used.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main>
    <h1>Guardian consent</h1>

    <p v-if="loadError">This consent link is not valid.</p>

    <template v-else-if="context">
      <p v-if="submitted || context.state === 'CONSENTED'">
        Thank you — consent is recorded for {{ context.menteeEmail }}. They can now use Reinvent.
      </p>
      <p v-else-if="context.state === 'EXPIRED'">
        This consent link has expired. Please ask {{ context.menteeEmail }} to request a fresh one.
      </p>
      <form v-else @submit.prevent="submit">
        <p>{{ context.purpose }}</p>
        <p>You are consenting for: {{ context.menteeEmail }}</p>
        <label>
          <input v-model="confirmGuardian" type="checkbox" />
          I confirm I am {{ context.guardianName }}, this teen's guardian.
        </label>
        <label>
          <input v-model="agreeToTerms" type="checkbox" />
          I consent to their use of Reinvent.
        </label>
        <button type="submit" :disabled="!confirmGuardian || !agreeToTerms || submitting">Give consent</button>
        <p v-if="submitError">{{ submitError }}</p>
      </form>
    </template>
  </main>
</template>
