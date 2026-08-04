export default defineEventHandler(async (event) => {
  // The application carries the applicant's headline and bio, so the body has to be
  // forwarded — the backend requires both.
  const body = await readBody(event)
  return proxyToBackend(event, '/api/mentor-applications', { method: 'POST', body })
})
