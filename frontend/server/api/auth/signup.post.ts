export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  return proxyToBackend(event, '/api/auth/signup', { method: 'POST', body })
})
