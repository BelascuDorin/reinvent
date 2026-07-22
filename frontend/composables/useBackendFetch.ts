/**
 * A fetcher for our BFF (server/api) routes, narrowed to (url) => Promise<unknown>.
 *
 * The full typed-$fetch signature scores every Nitro route and exceeds TypeScript's
 * instantiation-depth limit; these calls return the backend's contract, which Nuxt
 * can't type anyway. Built on useRequestFetch so the caller's session cookie is
 * forwarded during SSR. Callers cast the result to the expected shape.
 */
export function useBackendFetch(): (url: string) => Promise<unknown> {
  return useRequestFetch() as (url: string) => Promise<unknown>
}
