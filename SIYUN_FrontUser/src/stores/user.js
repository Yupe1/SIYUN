import { defineStore } from 'pinia'
import {
  getProfile,
  identify as identifyApi,
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
} from '@/api/user'
import { clearSessionCookie, pickResult } from '@/utils/request'

const USER_KEY = 'SIYUN_FRONT_USER'

export const useUserStore = defineStore('frontUser', {
  state: () => ({
    user: null,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.user?.id),
    displayName: (state) => state.user?.nickName || state.user?.stuTel || '未登录',
  },
  actions: {
    hydrate() {
      const cached = uni.getStorageSync(USER_KEY)
      if (cached) {
        this.user = cached
      }
    },
    persist(user) {
      this.user = user
      if (user) {
        uni.setStorageSync(USER_KEY, user)
      } else {
        uni.removeStorageSync(USER_KEY)
      }
    },
    async login(form) {
      const response = await loginApi(form)
      const user = pickResult(response, 'loginUser', null)
      this.persist(user)
      return user
    },
    async register(form) {
      return registerApi(form)
    },
    async refresh() {
      const response = await getProfile()
      const user = pickResult(response, 'frontUser', null)
      if (user) {
        this.persist(user)
      }
      return user
    },
    async identify(form) {
      const response = await identifyApi(form)
      const user = pickResult(response, 'frontUser', null)
      if (user) {
        this.persist(user)
      }
      return user
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.persist(null)
      }
    },
    expireSession() {
      clearSessionCookie()
      this.persist(null)
    },
  },
})
