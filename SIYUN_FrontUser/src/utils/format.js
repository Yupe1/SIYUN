import { BASE_URL } from './request'

export function assetUrl(url) {
  if (!url) {
    return ''
  }
  if (/^https?:\/\//.test(url) || url.startsWith('data:') || url.startsWith('/static/')) {
    return url
  }
  const prefix = BASE_URL === '/api' ? '' : BASE_URL
  return `${prefix}${url.startsWith('/') ? url : `/${url}`}`
}

export function money(value) {
  const number = Number(value)
  if (Number.isNaN(number)) {
    return '0'
  }
  return number % 1 === 0 ? String(number) : number.toFixed(2)
}

export function compactNumber(value) {
  const number = Number(value || 0)
  if (number >= 10000) {
    return `${(number / 10000).toFixed(1)}w`
  }
  return String(number)
}

export function dateText(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 16)
}
