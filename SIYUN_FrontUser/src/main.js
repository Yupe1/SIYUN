import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { useUserStore } from '@/stores/user'
import { h5ReplaceTo } from '@/utils/navigation'
import { setUnauthorizedHandler } from '@/utils/request'

const LOGIN_URL = '/pages/auth/login'
let sessionExpiredRedirecting = false

function getCurrentRoute() {
  try {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    return current?.route || ''
  } catch (error) {
    return ''
  }
}

function goLogin() {
  if (h5ReplaceTo(LOGIN_URL, { reload: true })) {
    sessionExpiredRedirecting = false
    return
  }

  uni.reLaunch({
    url: LOGIN_URL,
    fail: () => uni.redirectTo({ url: LOGIN_URL }),
    complete: () => {
      sessionExpiredRedirecting = false
    },
  })
}

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)

  const userStore = useUserStore(pinia)
  setUnauthorizedHandler(() => {
    userStore.expireSession()
    if (getCurrentRoute() === 'pages/auth/login' || sessionExpiredRedirecting) {
      return
    }
    sessionExpiredRedirecting = true
    uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
    setTimeout(goLogin, 500)
  })

  return {
    app,
  }
}
