export default defineEventHandler(async (event) => {
  return proxyToBackend(event, '/api/mentor-profiles/me')
})
