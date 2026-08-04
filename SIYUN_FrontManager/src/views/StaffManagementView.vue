<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref(null)
const deptFilter = ref(null)
const depts = ref([])
const roles = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const form = ref(newStaffForm())

const roleId = computed(() => Number(route.meta.staffRoleId))
const staffLabel = computed(() => route.meta.staffLabel || '人员')

const genderOptions = [
  { label: '女', value: 0 },
  { label: '男', value: 1 },
]

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁言', value: 1 },
  { label: '封停', value: 2 },
  { label: '注销', value: 3 },
]

function newStaffForm() {
  return {
    name: '',
    tel: '',
    email: '',
    gender: 0,
    chinaId: '',
    birth: '',
    deptId: null,
    status: 0,
    level: '',
    salary: '',
    roleIds: [],
    remark: '',
  }
}

async function loadOptions() {
  const data = await http.get('/api/admin/staff-options')
  depts.value = data.result?.depts || []
  roles.value = data.result?.roles || []
}

async function load() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      roleId: roleId.value,
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value !== null && statusFilter.value !== '') params.status = statusFilter.value
    if (deptFilter.value !== null && deptFilter.value !== '') params.deptId = deptFilter.value
    const data = await http.get('/api/admin/staff-users', { params })
    const resultPage = data.result?.page || {}
    rows.value = resultPage.records || []
    total.value = resultPage.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
  load()
}

function resetFilters() {
  keyword.value = ''
  statusFilter.value = null
  deptFilter.value = null
  currentPage.value = 1
  load()
}

function openCreate() {
  dialogMode.value = 'create'
  form.value = newStaffForm()
  form.value.roleIds = [roleId.value]
  if (depts.value.length === 1) form.value.deptId = depts.value[0].id
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    name: row.name || '',
    tel: row.tel || '',
    email: row.email || '',
    gender: row.gender ?? 0,
    chinaId: row.chinaId || '',
    birth: row.birth || '',
    deptId: row.deptId ?? null,
    status: row.status ?? 0,
    level: row.level ?? '',
    salary: row.salary ?? '',
    roleIds: [...(row.roleIds || [])],
    remark: row.remark || '',
  }
  dialogVisible.value = true
}

function validate() {
  if (!form.value.name.trim()) return '请填写姓名'
  if (!form.value.tel.trim() || form.value.tel.trim().length < 6) return '请填写正确的手机号'
  if (!form.value.deptId) return '请选择所属部门'
  if (!form.value.roleIds.length) return '请至少选择一个角色'
  return ''
}

function optionalNumber(value) {
  return value === '' || value === null || typeof value === 'undefined' ? null : Number(value)
}

async function submit() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: form.value.name.trim(),
      tel: form.value.tel.trim(),
      email: form.value.email.trim(),
      gender: form.value.gender,
      chinaId: form.value.chinaId.trim(),
      birth: form.value.birth || null,
      deptId: form.value.deptId,
      status: form.value.status,
      level: optionalNumber(form.value.level),
      salary: optionalNumber(form.value.salary),
      roleIds: form.value.roleIds,
      remark: form.value.remark.trim(),
    }
    if (dialogMode.value === 'create') {
      await http.post('/api/admin/staff-users', payload)
      ElMessage.success('新增成功，初始密码为手机号后6位')
    } else {
      await http.put(`/api/admin/staff-users/${form.value.id}`, payload)
      ElMessage.success('人员信息已更新')
    }
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function deleteRow(row) {
  await ElMessageBox.confirm(`确认删除人员“${row.name}”？`, '删除确认', { type: 'warning' })
  await http.delete(`/api/admin/staff-users/${row.id}`)
  ElMessage.success('人员已删除')
  await load()
}

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value
}

function deptLabel(dept) {
  return `${dept.deptName}${dept.status === 1 ? '' : '（停用）'}`
}

function changePage(page) {
  currentPage.value = page
  load()
}

function changePageSize(size) {
  pageSize.value = size
  currentPage.value = 1
  load()
}

onMounted(async () => {
  await loadOptions()
  await load()
})

watch(roleId, () => {
  currentPage.value = 1
  load()
})
</script>

<template>
  <section class="table-page">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="姓名/手机号"
          clearable
          class="filter-item"
          @keyup.enter="search"
        />
        <el-select v-model="deptFilter" placeholder="所属部门" clearable class="filter-item">
          <el-option v-for="dept in depts" :key="dept.id" :label="deptLabel(dept)" :value="dept.id" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="账号状态" clearable class="filter-item">
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <el-button v-if="auth.hasPerm('admin:staff:add')" type="primary" :icon="Plus" @click="openCreate">
        新增{{ staffLabel }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe height="calc(100vh - 245px)">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="name" label="姓名" width="120" show-overflow-tooltip />
      <el-table-column prop="tel" label="手机号" width="135" />
      <el-table-column prop="gender" label="性别" width="70">
        <template #default="{ row }">{{ optionLabel(genderOptions, row.gender) }}</template>
      </el-table-column>
      <el-table-column prop="deptName" label="部门" width="130" show-overflow-tooltip />
      <el-table-column prop="roleNames" label="角色" width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.roleNames || []).join('、') }}</template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ optionLabel(statusOptions, row.status) }}</template>
      </el-table-column>
      <el-table-column prop="level" label="级别" width="80" />
      <el-table-column prop="salary" label="薪资" width="100" />
      <el-table-column label="操作" fixed="right" width="140">
        <template #default="{ row }">
          <el-button v-if="auth.hasPerm('admin:staff:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="auth.hasPerm('admin:staff:delete')" link type="danger" @click="deleteRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="changePage"
        @size-change="changePageSize"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? `新增${staffLabel}` : `编辑${staffLabel}`"
      width="760px"
    >
      <el-form label-width="90px" class="staff-dialog-form">
        <el-form-item label="姓名">
          <el-input v-model="form.name" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.tel" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" class="full-control">
            <el-option v-for="option in genderOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="255" />
        </el-form-item>
        <el-form-item label="身份证">
          <el-input v-model="form.chinaId" maxlength="18" />
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birth" type="date" value-format="YYYY-MM-DD" class="full-control" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="form.deptId" filterable class="full-control" placeholder="请选择部门">
            <el-option
              v-for="dept in depts"
              :key="dept.id"
              :label="deptLabel(dept)"
              :value="dept.id"
              :disabled="dept.status !== 1 && dept.id !== form.deptId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="form.status" class="full-control">
            <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-input v-model="form.level" inputmode="numeric" />
        </el-form-item>
        <el-form-item label="薪资">
          <el-input v-model="form.salary" inputmode="decimal">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple filterable class="full-control" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" class="staff-form-wide">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
