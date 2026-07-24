export default defineEventHandler(async (event) => {
  const body = await readBody(event)
  return proxyToBackend(event, '/api/mentor-profiles/me', { method: 'PUT', body })
})
