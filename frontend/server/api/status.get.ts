// BFF route: proxies the backend status endpoint so the browser talks only to
// the Nuxt origin (no CORS, and the backend URL stays server-side). Later
// authenticated calls will forward the auth-session cookie the same way.
export default defineEventHandler(async (): Promise<unknown> => {
  const { backendBaseUrl } = useRuntimeConfig()
  return await $fetch(`${backendBaseUrl}/api/status`)
})
