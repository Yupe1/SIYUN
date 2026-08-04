<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const loading = ref(false)
const submitting = ref(false)
const coupons = ref([])
const users = ref([])
const boundCouponIds = ref(new Set())
const grantedCouponKeys = ref(new Set())
const form = reactive({ couponId: null, userId: null })

const selectedCoupon = computed(() => coupons.value.find((item) => item.id === form.couponId))
const selectedUser = computed(() => users.value.find((item) => item.id === form.userId))

function userLabel(user) {
  return `${user.nickName || '未设置昵称'}（${user.stuTel}）`
}

function couponLabel(coupon) {
  const bindingStatus = boundCouponIds.value.has(coupon.id) ? '' : '，未绑定商品/课程'
  const ownedStatus = hasGranted(coupon.id) ? '，该用户已领取' : ''
  return `${coupon.couponName}（${coupon.amount}元${bindingStatus}${ownedStatus}）`
}

function hasGranted(couponId) {
  return Boolean(form.userId && grantedCouponKeys.value.has(`${form.userId}-${couponId}`))
}

async function loadOptions() {
  loading.value = true
  try {
    const data = await http.get('/api/admin/coupon-grant-options')
    coupons.value = data.result?.coupons || []
    users.value = data.result?.users || []
    boundCouponIds.value = new Set(data.result?.boundCouponIds || [])
    grantedCouponKeys.value = new Set(
      (data.result?.grantedCouponUsers || [])
        .map((item) => `${item.userId}-${item.couponId}`),
    )
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.userId) {
    ElMessage.warning('请选择接收优惠券的用户')
    return
  }
  if (!form.couponId) {
    ElMessage.warning('请选择要赠送的优惠券')
    return
  }
  if (hasGranted(form.couponId)) {
    ElMessage.warning('该用户已经领取过这张优惠券，不能重复赠送')
    return
  }
  submitting.value = true
  try {
    await http.post('/api/admin/coupon-users/grant', form)
    ElMessage.success('优惠券赠送成功')
    await loadOptions()
    form.userId = null
    form.couponId = null
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <section v-loading="loading" class="course-create-page">
    <el-form label-width="110px" class="course-form">
      <section class="panel form-panel">
        <header>赠送优惠券</header>
        <div class="coupon-operation-form">
          <el-alert
            title="同一用户对同一张优惠券只能领取一次，使用或过期后也不能重复赠送。"
            type="info"
            :closable="false"
            show-icon
          />
          <el-form-item label="接收用户" required>
            <el-select v-model="form.userId" filterable clearable placeholder="按昵称或手机号搜索" class="full-control">
              <el-option v-for="user in users" :key="user.id" :label="userLabel(user)" :value="user.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="优惠券" required>
            <el-select v-model="form.couponId" filterable clearable placeholder="请选择当前可用优惠券" class="full-control">
              <el-option
                v-for="coupon in coupons"
                :key="coupon.id"
                :label="couponLabel(coupon)"
                :value="coupon.id"
                :disabled="!boundCouponIds.has(coupon.id) || hasGranted(coupon.id)"
              />
            </el-select>
          </el-form-item>

          <el-descriptions v-if="selectedUser || selectedCoupon" :column="1" border class="coupon-preview">
            <el-descriptions-item v-if="selectedUser" label="接收用户">{{ userLabel(selectedUser) }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedCoupon" label="优惠券">{{ selectedCoupon.couponName }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedCoupon" label="面值">{{ selectedCoupon.amount }} 元</el-descriptions-item>
            <el-descriptions-item v-if="selectedCoupon" label="有效期">
              {{ selectedCoupon.startTime }} 至 {{ selectedCoupon.endTime }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <div class="form-actions">
        <el-button type="primary" :loading="submitting" @click="submit">确认赠送</el-button>
      </div>
    </el-form>
  </section>
</template>
