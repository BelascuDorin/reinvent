export default defineEventHandler(async (event) => {
  return proxyToBackend(event, '/api/fields')
})
