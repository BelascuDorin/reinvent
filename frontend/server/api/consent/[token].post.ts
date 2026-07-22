export default defineEventHandler(async (event) => {
  const token = getRouterParam(event, 'token')
  const body = await readBody(event)
  return proxyToBackend(event, `/api/consent/${token}`, { method: 'POST', body })
})
