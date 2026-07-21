<script setup lang="ts">
const email = ref('')
const password = ref('')
const dateOfBirth = ref('')
const error = ref<string | null>(null)

async function submit() {
  error.value = null
  try {
    await $fetch('/api/auth/signup', {
      method: 'POST',
      body: { email: email.value, password: password.value, dateOfBirth: dateOfBirth.value },
    })
    await navigateTo('/login')
  } catch (e: unknown) {
    error.value = 'Could not sign up — the email may already be in use.'
  }
}
</script>

<template>
  <main>
    <h1>Sign up</h1>
    <form @submit.prevent="submit">
      <label>Email <input v-model="email" type="email" required /></label>
      <label>Password <input v-model="password" type="password" minlength="8" required /></label>
      <label>Date of birth <input v-model="dateOfBirth" type="date" required /></label>
      <button type="submit">Create account</button>
    </form>
    <p v-if="error">{{ error }}</p>
  </main>
</template>
