export default defineEventHandler(async (event) => {
  const id = getRouterParam(event, 'id')
  const body = await readBody(event)
  return proxyToBackend(event, `/api/reviewer/applications/${id}/reject`, { method: 'POST', body })
})
