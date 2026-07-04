export function h5ReplaceTo(url, options = {}) {
  const { reload = false } = options

  // #ifdef H5
  const { origin, pathname, search } = window.location
  const query = reload ? `?_nav=${Date.now()}` : search
  window.location.replace(`${origin}${pathname}${query}#${url}`)
  return true
  // #endif

  return false
}
