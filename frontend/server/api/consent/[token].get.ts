export default defineEventHandler(async (event) => {
  const token = getRouterParam(event, 'token')
  return proxyToBackend(event, `/api/consent/${token}`)
})
