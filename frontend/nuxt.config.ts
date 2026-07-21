// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  devtools: { enabled: true },

  runtimeConfig: {
    // Server-only: base URL of the Reinvent backend. The public marketplace is
    // rendered server-side (SSR), so pages never call the backend directly from
    // the browser — they go through the Nuxt server routes (BFF) in server/api.
    backendBaseUrl: 'http://localhost:8080',
  },
})
