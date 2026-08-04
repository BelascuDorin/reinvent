export default defineEventHandler(async (event) => {
  const mentorUserId = getRouterParam(event, 'mentorUserId')
  return proxyToBackend(event, `/api/mentors/${mentorUserId}`)
})
