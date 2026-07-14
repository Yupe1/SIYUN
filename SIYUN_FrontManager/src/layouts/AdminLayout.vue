<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, Refresh, SwitchButton } from '@element-plus/icons-vue'
import { menuGroups } from '@/menu'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const visibleGroups = computed(() =>
  menuGroups
    .map((group) => ({
      ...group,
      children: group.children.filter((item) => auth.hasAny(item.perms)),
    }))
    .filter((group) => group.children.length > 0),
)

const activeGroup = computed(() => {
  return visibleGroups.value.find((group) => route.path.startsWith(group.base)) || visibleGroups.value[0]
})

const pageTitle = computed(() => route.meta.title || activeGroup.value?.title || '系统首页')

async function logout() {
  await ElMessageBox.confirm('确认退出后台管理系统？', '退出登录', { type: 'warning' })
  await auth.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="admin-shell">
    <header class="topbar">
      <div class="brand">
        <div class="brand-logo">SI</div>
        <strong>思云课堂后台业务管理系统</strong>
      </div>
      <div class="top-actions">
        <el-avatar :size="30" :src="auth.loginUser?.avataUrl">{{ auth.loginUser?.name?.slice(0, 1) }}</el-avatar>
        <span class="user-name">{{ auth.loginUser?.name || 'admin' }}</span>
        <el-divider direction="vertical" />
        <el-button :icon="HomeFilled" circle text @click="router.push('/dashboard')" />
        <el-button :icon="Refresh" circle text @click="router.go(0)" />
        <el-button :icon="SwitchButton" circle text @click="logout" />
      </div>
    </header>

    <aside class="group-rail">
      <button
        v-for="group in visibleGroups"
        :key="group.key"
        class="rail-item"
        :class="{ active: activeGroup?.key === group.key }"
        @click="router.push(group.children[0].path)"
      >
        <component :is="group.icon" class="rail-icon" />
        <span>{{ group.title }}</span>
      </button>
    </aside>

    <aside class="subnav">
      <div class="subnav-title">{{ activeGroup?.title || '系统' }}管理</div>
      <router-link
        v-for="item in activeGroup?.children || []"
        :key="item.path"
        class="subnav-item"
        :class="{ active: route.path === item.path }"
        :to="item.path"
      >
        <span class="dot">•</span>{{ item.title }}
      </router-link>
    </aside>

    <main class="main-panel">
      <div class="page-head">
        <div class="tab-title">{{ pageTitle }}</div>
        <el-button :icon="Refresh" @click="router.go(0)">刷新</el-button>
      </div>
      <RouterView />
    </main>
  </div>
</template>
