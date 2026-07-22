export default defineEventHandler(async (event) => {
  const id = getRouterParam(event, 'id')
  return proxyToBackend(event, `/api/reviewer/applications/${id}/approve`, { method: 'POST' })
})
