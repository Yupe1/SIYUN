let baseURL = 'http://localhost:8081'

// #ifdef H5
baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
// #endif

// #ifndef H5
baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
// #endif

export const BASE_URL = baseURL

const COOKIE_KEY = 'SIYUN_FRONT_SESSION_COOKIE'
const NOT_LOGIN_CODE = 1006
let unauthorizedHandler = null

function normalizeUrl(url) {
  if (/^https?:\/\//.test(url)) {
    return url
  }
  return `${BASE_URL}${url}`
}

function readCookie() {
  try {
    return uni.getStorageSync(COOKIE_KEY) || ''
  } catch (error) {
    return ''
  }
}

function saveCookie(headers = {}) {
  const setCookie = headers['Set-Cookie'] || headers['set-cookie']
  if (!setCookie) {
    return
  }

  const raw = Array.isArray(setCookie) ? setCookie.join(',') : setCookie
  const cookie = raw
    .split(',')
    .map((item) => item.split(';')[0].trim())
    .filter(Boolean)
    .join('; ')

  if (cookie) {
    uni.setStorageSync(COOKIE_KEY, cookie)
  }
}

export function clearSessionCookie() {
  uni.removeStorageSync(COOKIE_KEY)
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = typeof handler === 'function' ? handler : null
}

export function isSessionExpiredError(error) {
  return Boolean(error?.sessionExpired || error?.code === NOT_LOGIN_CODE)
}

export function isNotFoundError(error) {
  return Number(error?.statusCode) === 404 || String(error?.message || '').includes('404')
}

function isUnauthorizedPayload(payload) {
  return payload?.errorCode === NOT_LOGIN_CODE
}

function notifyUnauthorized(payload) {
  if (!unauthorizedHandler) {
    return
  }
  unauthorizedHandler(payload)
}

function createRequestError(message, payload = {}) {
  const error = new Error(message || '请求失败')
  error.code = payload.errorCode
  error.statusCode = payload.statusCode
  error.sessionExpired = isUnauthorizedPayload(payload)
  return error
}

export function request(options) {
  const { url, method = 'GET', data, header = {}, loading = false } = options
  const cookie = readCookie()

  if (loading) {
    uni.showLoading({
      title: '加载中',
      mask: false,
    })
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: normalizeUrl(url),
      method,
      data,
      withCredentials: true,
      header: {
        'content-type': 'application/json',
        ...(cookie ? { Cookie: cookie } : {}),
        ...header,
      },
      success: (response) => {
        saveCookie(response.header)
        const payload = response.data
        if (response.statusCode < 200 || response.statusCode >= 300) {
          const error = createRequestError(payload?.msg || `请求失败：${response.statusCode}`, {
            ...(payload || {}),
            statusCode: response.statusCode,
          })
          if (response.statusCode === 401) {
            error.code = NOT_LOGIN_CODE
            error.sessionExpired = true
          }
          if (error.sessionExpired) {
            notifyUnauthorized(payload || { errorCode: NOT_LOGIN_CODE })
          }
          reject(error)
          return
        }
        if (payload && typeof payload.errorCode === 'number' && payload.errorCode !== 0) {
          const error = createRequestError(payload.msg || '请求失败', payload)
          if (error.sessionExpired) {
            notifyUnauthorized(payload)
          }
          reject(error)
          return
        }
        resolve(payload)
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络异常'))
      },
      complete: () => {
        if (loading) {
          uni.hideLoading()
        }
      },
    })
  })
}

export function pickResult(response, key, fallback = null) {
  return response?.result?.[key] ?? fallback
}
