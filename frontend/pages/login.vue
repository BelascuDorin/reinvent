<script setup lang="ts">
const email = ref('')
const password = ref('')
const error = ref<string | null>(null)

async function submit() {
  error.value = null
  try {
    await $fetch('/api/auth/login', {
      method: 'POST',
      body: { email: email.value, password: password.value },
    })
    await navigateTo('/account')
  } catch (e: unknown) {
    error.value = 'Invalid email or password.'
  }
}
</script>

<template>
  <main>
    <h1>Log in</h1>
    <form @submit.prevent="submit">
      <label>Email <input v-model="email" type="email" required /></label>
      <label>Password <input v-model="password" type="password" required /></label>
      <button type="submit">Log in</button>
    </form>
    <p v-if="error">{{ error }}</p>
  </main>
</template>
