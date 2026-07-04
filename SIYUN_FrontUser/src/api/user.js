import { clearSessionCookie, request } from '@/utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'POST',
    data,
    loading: true,
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'POST',
    data,
    loading: true,
  })
}

export async function logout() {
  try {
    return await request({
      url: '/user/logout',
      method: 'POST',
    })
  } finally {
    clearSessionCookie()
  }
}

export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'POST',
    data,
    loading: true,
  })
}

export function getProfile() {
  return request({
    url: '/user/me',
    method: 'GET',
  })
}

export function identify(data) {
  return request({
    url: '/siyun/identify',
    method: 'PUT',
    data,
    loading: true,
  })
}
