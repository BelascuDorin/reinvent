export default defineEventHandler(async (event) => {
  // Public discovery: the Field and language a visitor searched for are passed
  // straight through, and the backend decides what "no filter" means.
  const { field, language } = getQuery(event)
  return proxyToBackend(event, '/api/mentors', { query: { field, language } })
})
