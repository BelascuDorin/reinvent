<script setup lang="ts">
interface Account {
  email: string
  roles: string[]
  minor: boolean
  guardianConsentStatus: string
}

// useRequestFetch forwards the incoming request's cookies during SSR, so the
// account renders server-side for a signed-in User. We narrow it to a plain
// (url) => Promise<unknown> signature: these BFF calls return the backend's
// contract (which Nuxt can't type anyway), and the full typed-$fetch signature
// scores every Nitro route, blowing TS's instantiation-depth limit.
const request = useRequestFetch() as (url: string) => Promise<unknown>
const { data: account } = await useAsyncData<Account>('me', () =>
  request('/api/accounts/me') as Promise<Account>,
)

async function logout() {
  await $fetch('/api/auth/logout', { method: 'POST' })
  await navigateTo('/login')
}
</script>

<template>
  <main>
    <h1>Your account</h1>
    <div v-if="account">
      <p>Email: {{ account.email }}</p>
      <p>Roles: {{ account.roles.join(', ') }}</p>
      <p>Minor: {{ account.minor }}</p>
      <p v-if="account.minor">Guardian consent: {{ account.guardianConsentStatus }}</p>
      <button @click="logout">Log out</button>
    </div>
    <p v-else>
      You are not signed in. <NuxtLink to="/login">Log in</NuxtLink>
    </p>
  </main>
</template>
