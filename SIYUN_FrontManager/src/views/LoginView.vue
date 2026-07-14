<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { firstAllowedPath } from '@/menu'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  tel: '',
  password: '',
})

async function submit() {
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.replace(firstAllowedPath(auth))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">SI</div>
        <div>
          <h1>思云课堂后台业务管理系统</h1>
          <p>教师、运营、人事、管理员统一入口</p>
        </div>
      </div>

      <el-form label-position="top" @keyup.enter="submit">
        <el-form-item label="手机号">
          <el-input v-model="form.tel" size="large" placeholder="后台账号手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button class="login-btn" type="primary" size="large" :loading="loading" @click="submit">登录后台</el-button>
      </el-form>
    </div>
  </div>
</template>
