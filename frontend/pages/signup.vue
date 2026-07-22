<script setup lang="ts">
const email = ref('')
const password = ref('')
const dateOfBirth = ref('')
const guardianName = ref('')
const guardianEmail = ref('')
const error = ref<string | null>(null)

// A minor (under 18) must name a Guardian; an adult must not. We mirror the
// backend rule here to show the right fields — the backend, via the Clock port,
// remains the source of truth.
const isMinor = computed(() => {
  if (!dateOfBirth.value) return false
  const dob = new Date(dateOfBirth.value)
  const eighteenth = new Date(dob.getFullYear() + 18, dob.getMonth(), dob.getDate())
  return eighteenth > new Date()
})

async function submit() {
  error.value = null
  try {
    await $fetch('/api/auth/signup', {
      method: 'POST',
      body: {
        email: email.value,
        password: password.value,
        dateOfBirth: dateOfBirth.value,
        ...(isMinor.value
          ? { guardianName: guardianName.value, guardianEmail: guardianEmail.value }
          : {}),
      },
    })
    await navigateTo('/login')
  } catch {
    error.value = 'Could not sign up — the email may already be in use, or the guardian details are missing.'
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

      <fieldset v-if="isMinor">
        <legend>Your guardian</legend>
        <p>Because you're under 18, your guardian must consent before you can use Reinvent.</p>
        <label>Guardian name <input v-model="guardianName" type="text" required /></label>
        <label>Guardian email <input v-model="guardianEmail" type="email" required /></label>
      </fieldset>

      <button type="submit">Create account</button>
    </form>
    <p v-if="error">{{ error }}</p>
  </main>
</template>
