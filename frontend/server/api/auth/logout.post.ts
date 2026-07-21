export default defineEventHandler(async (event) => {
  return proxyToBackend(event, '/api/auth/logout', { method: 'POST' })
})
