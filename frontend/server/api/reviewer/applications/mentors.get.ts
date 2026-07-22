export default defineEventHandler(async (event) => {
  return proxyToBackend(event, '/api/reviewer/applications/mentors')
})
