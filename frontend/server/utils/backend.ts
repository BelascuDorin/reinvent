import type { H3Event } from 'h3'

// Proxies a request to the Reinvent backend, bridging the session cookie both
// ways: it forwards the browser's cookie to the backend, and relays the
// backend's Set-Cookie back to the browser. This keeps the browser talking only
// to the Nuxt origin while the backend owns the session.
export async function proxyToBackend(
  event: H3Event,
  path: string,
  options: { method?: string; body?: unknown; query?: Record<string, unknown> } = {},
) {
  const { backendBaseUrl } = useRuntimeConfig()
  const cookie = getRequestHeader(event, 'cookie')

  const response = await $fetch.raw(`${backendBaseUrl}${path}`, {
    method: (options.method ?? 'GET') as never,
    body: options.body as never,
    query: options.query,
    headers: cookie ? { cookie } : undefined,
    ignoreResponseError: true,
  })

  for (const value of response.headers.getSetCookie?.() ?? []) {
    appendResponseHeader(event, 'set-cookie', value)
  }
  setResponseStatus(event, response.status)
  return response._data
}
