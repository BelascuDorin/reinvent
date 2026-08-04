<script setup lang="ts">
// The two things an application carries. Shared by the first application and the reapply
// path so the two can't drift apart.
const headline = defineModel<string>('headline', { required: true })
const bio = defineModel<string>('bio', { required: true })
</script>

<template>
  <p>
    <label>
      Headline
      <input v-model="headline" type="text" maxlength="200" required
        placeholder="e.g. Staff Engineer at a hospital" />
    </label>
  </p>
  <p>
    <label>
      About you
      <textarea v-model="bio" maxlength="5000" required
        placeholder="What you do, and what you could help a Mentee with." />
    </label>
  </p>
</template>
