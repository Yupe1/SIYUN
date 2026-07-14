import { defineStore } from 'pinia'
import http from '@/api/http'

const STORAGE_KEY = 'siyun_admin_auth'

function loadStored() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const stored = loadStored()
    return {
      loginUser: stored.loginUser || null,
      roles: stored.roles || [],
      perms: stored.perms || [],
      permissions: stored.permissions || [],
      checkedSession: false,
    }
  },
  getters: {
    isLogin: (state) => Boolean(state.loginUser?.id),
    roleNames: (state) => state.roles.map((role) => role.roleName || role.roleKey).join('、'),
  },
  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          loginUser: this.loginUser,
          roles: this.roles,
          perms: this.perms,
          permissions: this.permissions,
        }),
      )
    },
    async login(form) {
      const data = await http.post('/api/admin/sys/login', form)
      const result = data.result || {}
      this.loginUser = result.loginUser
      this.roles = result.roles || []
      this.perms = result.perms || []
      this.permissions = result.permissions || []
      this.checkedSession = true
      this.persist()
    },
    clearAuth() {
      this.loginUser = null
      this.roles = []
      this.perms = []
      this.permissions = []
      this.checkedSession = false
      localStorage.removeItem(STORAGE_KEY)
    },
    async refreshMe() {
      const data = await http.get('/api/admin/sys/me')
      const result = data.result || {}
      this.loginUser = result.loginUser || this.loginUser
      this.perms = result.perms || this.perms
      this.persist()
    },
    async ensureSession() {
      if (!this.isLogin) return false
      if (this.checkedSession) return true
      try {
        await this.refreshMe()
        this.checkedSession = true
        return true
      } catch {
        this.clearAuth()
        return false
      }
    },
    async logout() {
      try {
        await http.post('/api/admin/sys/logout')
      } finally {
        this.clearAuth()
      }
    },
    hasPerm(perm) {
      return this.perms.includes(perm) || this.perms.some((item) => item.endsWith('*') && perm.startsWith(item.slice(0, -1)))
    },
    hasAny(perms = []) {
      return perms.length === 0 || perms.some((perm) => this.hasPerm(perm))
    },
  },
})
