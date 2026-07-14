<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const roles = ref([])
const permissions = ref([])
const checkedIds = ref([])
const activeRole = ref(null)
const treeRef = ref()
const roleDialog = ref(false)
const roleForm = reactive({ id: null, roleName: '', roleKey: '', sortNum: 0, status: 1, remark: '' })

const treeData = computed(() => buildTree(permissions.value))

async function load() {
  loading.value = true
  try {
    const [roleRes, permRes] = await Promise.all([http.get('/api/admin/roles'), http.get('/api/admin/permissions')])
    roles.value = roleRes.result?.roles || []
    permissions.value = permRes.result?.permissions || []
    if (!activeRole.value && roles.value.length) selectRole(roles.value[0])
  } finally {
    loading.value = false
  }
}

async function selectRole(role) {
  activeRole.value = role
  const data = await http.get(`/api/admin/roles/${role.id}/permissions`)
  checkedIds.value = data.result?.permissionIds || []
  treeRef.value?.setCheckedKeys(checkedIds.value)
}

function openRole(row = null) {
  Object.assign(roleForm, row || { id: null, roleName: '', roleKey: '', sortNum: 0, status: 1, remark: '' })
  roleDialog.value = true
}

async function saveRole() {
  if (roleForm.id) {
    await http.put(`/api/admin/roles/${roleForm.id}`, roleForm)
  } else {
    await http.post('/api/admin/roles', roleForm)
  }
  ElMessage.success('角色已保存')
  roleDialog.value = false
  await load()
}

async function deleteRole(row) {
  await ElMessageBox.confirm(`确认删除角色 ${row.roleName}？`, '删除角色', { type: 'warning' })
  await http.delete(`/api/admin/roles/${row.id}`)
  ElMessage.success('角色已删除')
  if (activeRole.value?.id === row.id) activeRole.value = null
  await load()
}

async function savePermissions() {
  const ids = treeRef.value.getCheckedKeys(false)
  await http.put(`/api/admin/roles/${activeRole.value.id}/permissions`, { permissionIds: ids })
  ElMessage.success('角色权限已保存')
}

function buildTree(list) {
  const map = new Map()
  list.forEach((item) => map.set(item.id, { ...item, label: `${item.name}｜${item.perms}`, children: [] }))
  const roots = []
  map.forEach((item) => {
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId).children.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="role-page">
    <section class="role-list panel">
      <header>
        <span>角色列表</span>
        <el-button v-if="auth.hasPerm('admin:role:add')" type="primary" :icon="Plus" @click="openRole()">新增</el-button>
      </header>
      <el-table :data="roles" height="calc(100vh - 230px)" border highlight-current-row @row-click="selectRole">
        <el-table-column prop="roleName" label="角色" width="110" />
        <el-table-column prop="roleKey" label="role_key" width="120" />
        <el-table-column prop="status" label="状态" width="70" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="auth.hasPerm('admin:role:update')" link type="primary" @click.stop="openRole(row)">编辑</el-button>
            <el-button v-if="auth.hasPerm('admin:role:delete')" link type="danger" @click.stop="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="permission-tree panel">
      <header>
        <span>{{ activeRole ? `${activeRole.roleName} 权限` : '角色权限' }}</span>
        <el-button v-if="activeRole && auth.hasPerm('admin:role:permission')" type="primary" @click="savePermissions">保存授权</el-button>
      </header>
      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="id"
        show-checkbox
        default-expand-all
        :default-checked-keys="checkedIds"
        :props="{ label: 'label', children: 'children' }"
      />
    </section>

    <el-dialog v-model="roleDialog" title="角色" width="520px">
      <el-form label-width="100px">
        <el-form-item label="角色名称"><el-input v-model="roleForm.roleName" /></el-form-item>
        <el-form-item label="role_key"><el-input v-model="roleForm.roleKey" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="roleForm.sortNum" class="full-control" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roleForm.status" class="full-control">
            <el-option label="停用" :value="0" />
            <el-option label="正常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="roleForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
