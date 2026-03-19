<template>
  <div class="admin-dashboard">
    <ParticleBackground />

    <!-- 顶部导航栏 -->
    <div class="admin-header">
      <div class="header-left">
        <div class="logo-section">
          <el-icon :size="32" class="logo-icon"><Setting /></el-icon>
          <div class="logo-text">
            <h1 class="maritime-gradient-text">管理员控制台</h1>
            <p class="subtitle">Admin Dashboard</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info">
          <el-icon :size="20"><User /></el-icon>
          <span>{{ userStore.userInfo.username }}</span>
        </div>
        <el-button type="danger" @click="handleLogout" class="logout-btn">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card maritime-card maritime-float">
        <div class="stat-icon" style="background: var(--maritime-gradient-primary)">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.totalUsers }}</h3>
          <p>总用户数</p>
        </div>
      </div>

      <div class="stat-card maritime-card maritime-float">
        <div class="stat-icon" style="background: var(--maritime-gradient-gold)">
          <el-icon :size="32"><Ship /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.totalShips }}</h3>
          <p>总船舶数</p>
        </div>
      </div>

      <div class="stat-card maritime-card maritime-float">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%)">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ statistics.activeContracts }}</h3>
          <p>活跃合约</p>
        </div>
      </div>

      <div class="stat-card maritime-card maritime-float">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%)">
          <el-icon :size="32"><Money /></el-icon>
        </div>
        <div class="stat-content">
          <h3>¥{{ (statistics.totalAmount || 0).toLocaleString() }}</h3>
          <p>总交易额</p>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div class="section maritime-card">
      <div class="section-header">
        <h2 class="maritime-gradient-text">用户管理</h2>
        <MaritimeSelect
          v-model="userFilter"
          :options="roleOptions"
          placeholder="筛选角色"
          style="width: 150px"
          @change="loadUsers"
        />
      </div>

      <el-table :data="users" style="width: 100%" stripe height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="公司名称" min-width="150" />
        <el-table-column prop="creditScore" label="信用评分" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.creditScore"
              :color="getCreditColor(row.creditScore)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="其它操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editCreditScore(row)">
              <el-icon><Star /></el-icon>
              评分
            </el-button>
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="info" @click="viewUserDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="userPage"
          v-model:page-size="userPageSize"
          :total="userTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadUsers"
          @size-change="loadUsers"
        />
      </div>
    </div>

    <!-- 船舶管理 -->
    <div class="section maritime-card">
      <div class="section-header">
        <h2 class="maritime-gradient-text">船舶管理</h2>
      </div>

      <el-table :data="ships" style="width: 100%" stripe height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shipName" label="船舶名称" min-width="150" />
        <el-table-column prop="ownerName" label="船东" min-width="120" />
        <el-table-column prop="shipType" label="类型" width="120">
          <template #default="{ row }">
            {{ getShipTypeText(row.shipType) }}
          </template>
        </el-table-column>
        <el-table-column prop="dailyRent" label="日租金" min-width="120">
          <template #default="{ row }">
            ¥{{ row.dailyRent.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getShipStatusTagType(row.status)">
              {{ getShipStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <div class="rating">
              <el-icon color="#ffd700"><Star /></el-icon>
              <span>{{ row.rating }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="其它操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 'AVAILABLE' ? 'warning' : 'success'"
              @click="updateShipStatus(row)"
            >
              {{ row.status === 'AVAILABLE' ? '维护' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="shipPage"
          v-model:page-size="shipPageSize"
          :total="shipTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadShips"
          @size-change="loadShips"
        />
      </div>
    </div>

    <!-- 合约管理 -->
    <div class="section maritime-card">
      <div class="section-header">
        <h2 class="maritime-gradient-text">合约管理</h2>
      </div>

      <el-table :data="contracts" style="width: 100%" stripe height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shipName" label="船舶名称" min-width="150" />
        <el-table-column prop="ownerName" label="船东" min-width="120" />
        <el-table-column prop="renterName" label="租家" min-width="120" />
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="totalAmount" label="总金额" min-width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getContractStatusTagType(row.status)">
              {{ getContractStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="其它操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'IN_PROGRESS' || row.status === 'APPROVED'"
              size="small"
              type="danger"
              @click="terminateContract(row)"
            >
              终止
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="contractPage"
          v-model:page-size="contractPageSize"
          :total="contractTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadContracts"
          @size-change="loadContracts"
        />
      </div>
    </div>

    <!-- 数据统计图表 -->
    <div class="charts-grid">
      <div class="chart-card maritime-card">
        <h3>用户角色分布</h3>
        <div class="chart-content">
          <div
            v-for="(count, role) in statistics.usersByRole"
            :key="role"
            class="chart-item"
          >
            <div class="chart-label">{{ getRoleText(role) }}</div>
            <el-progress
              :percentage="getUserRolePercentage(role)"
              :color="getRoleColor(role)"
            >
              <span>{{ count }}</span>
            </el-progress>
          </div>
        </div>
      </div>

      <div class="chart-card maritime-card">
        <h3>船舶类型分布</h3>
        <div class="chart-content">
          <div
            v-for="(count, type) in statistics.shipsByType"
            :key="type"
            class="chart-item"
          >
            <div class="chart-label">{{ getShipTypeText(type) }}</div>
            <el-progress
              :percentage="getShipTypePercentage(type)"
              :color="getShipTypeColor(type)"
            >
              <span>{{ count }}</span>
            </el-progress>
          </div>
        </div>
      </div>
    </div>

    <!-- 信用评分对话框 -->
    <el-dialog
      v-model="showCreditDialog"
      title="修改信用评分"
      width="400px"
      class="maritime-dialog"
    >
      <el-form :model="creditForm" label-width="100px">
        <el-form-item label="用户">
          <el-input :value="selectedUser?.username" disabled />
        </el-form-item>
        <el-form-item label="当前评分">
          <el-input :value="selectedUser?.creditScore" disabled />
        </el-form-item>
        <el-form-item label="新评分">
          <el-slider
            v-model="creditForm.creditScore"
            :min="0"
            :max="100"
            :marks="{ 0: '0', 50: '50', 100: '100' }"
            show-input
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCreditScore" :loading="submitting">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Ship, Document, Money, Star, Setting, SwitchButton } from '@element-plus/icons-vue'
import {
  getStatistics,
  getAllUsers,
  updateUserStatus,
  updateCreditScore,
  getAllShips,
  updateShipStatus as updateShipStatusAPI,
  getAllContracts,
  forceTerminateContract
} from '@/api/admin'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeSelect from '@/components/MaritimeSelect.vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/auth')
  } catch (error) {
    // 用户取消
  }
}

const statistics = ref({
  totalUsers: 0,
  totalShips: 0,
  activeContracts: 0,
  totalAmount: 0,
  usersByRole: {},
  shipsByType: {},
  monthlyStatistics: []
})

const users = ref([])
const ships = ref([])
const contracts = ref([])

const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userFilter = ref('')

const shipPage = ref(1)
const shipPageSize = ref(10)
const shipTotal = ref(0)

const contractPage = ref(1)
const contractPageSize = ref(10)
const contractTotal = ref(0)

const showCreditDialog = ref(false)
const selectedUser = ref(null)
const submitting = ref(false)

const creditForm = reactive({
  creditScore: 100
})

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '船东', value: 'SHIP_OWNER' },
  { label: '租家', value: 'RENTER' },
  { label: '管理员', value: 'ADMIN' }
]

const getRoleText = (role) => {
  const map = {
    SHIP_OWNER: '船东',
    RENTER: '租家',
    ADMIN: '管理员'
  }
  return map[role] || role
}

const getRoleTagType = (role) => {
  const map = {
    SHIP_OWNER: 'primary',
    RENTER: 'success',
    ADMIN: 'danger'
  }
  return map[role] || 'info'
}

const getStatusText = (status) => {
  const map = {
    ACTIVE: '正常',
    LOCKED: '锁定',
    DISABLED: '禁用'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    ACTIVE: 'success',
    LOCKED: 'warning',
    DISABLED: 'danger'
  }
  return map[status] || 'info'
}

const getShipTypeText = (type) => {
  const map = {
    CONTAINER: '集装箱船',
    BULK: '散货船',
    TANKER: '油船',
    PASSENGER: '客船'
  }
  return map[type] || type
}

const getShipStatusText = (status) => {
  const map = {
    AVAILABLE: '可租',
    RENTED: '已租',
    MAINTENANCE: '维护中'
  }
  return map[status] || status
}

const getShipStatusTagType = (status) => {
  const map = {
    AVAILABLE: 'success',
    RENTED: 'warning',
    MAINTENANCE: 'info'
  }
  return map[status] || 'info'
}

const getContractStatusText = (status) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getContractStatusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    IN_PROGRESS: 'primary',
    COMPLETED: 'info',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getCreditColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const getRoleColor = (role) => {
  const map = {
    SHIP_OWNER: '#1e90ff',
    RENTER: '#67c23a',
    ADMIN: '#f56c6c'
  }
  return map[role] || '#909399'
}

const getShipTypeColor = (type) => {
  const map = {
    CONTAINER: '#1e90ff',
    BULK: '#67c23a',
    TANKER: '#e6a23c',
    PASSENGER: '#f56c6c'
  }
  return map[type] || '#909399'
}

const getUserRolePercentage = (role) => {
  const total = statistics.value.totalUsers
  const count = statistics.value.usersByRole[role] || 0
  return total > 0 ? Math.round((count / total) * 100) : 0
}

const getShipTypePercentage = (type) => {
  const total = statistics.value.totalShips
  const count = statistics.value.shipsByType[type] || 0
  return total > 0 ? Math.round((count / total) * 100) : 0
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载统计数据失败')
  }
}

const loadUsers = async () => {
  try {
    const res = await getAllUsers({
      page: userPage.value,
      size: userPageSize.value,
      role: userFilter.value || undefined
    })
    users.value = res.data.records
    userTotal.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载用户列表失败')
  }
}

const loadShips = async () => {
  try {
    const res = await getAllShips({
      page: shipPage.value,
      size: shipPageSize.value
    })
    ships.value = res.data.records
    shipTotal.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载船舶列表失败')
  }
}

const loadContracts = async () => {
  try {
    const res = await getAllContracts({
      page: contractPage.value,
      size: contractPageSize.value
    })
    contracts.value = res.data.records
    contractTotal.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载合约列表失败')
  }
}

const editCreditScore = (user) => {
  selectedUser.value = user
  creditForm.creditScore = user.creditScore
  showCreditDialog.value = true
}

const confirmCreditScore = async () => {
  submitting.value = true
  try {
    await updateCreditScore({
      userId: selectedUser.value.id,
      creditScore: creditForm.creditScore
    })
    ElMessage.success('信用评分更新成功')
    showCreditDialog.value = false
    loadUsers()
    loadStatistics()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleUserStatus = async (user) => {
  // 防止禁用管理员账号
  if (user.role === 'ADMIN') {
    ElMessage.warning('不能禁用管理员账号')
    return
  }

  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'

  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateUserStatus({
      userId: user.id,
      status: newStatus
    })
    ElMessage.success(`用户已${action}`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const updateShipStatus = async (ship) => {
  const newStatus = ship.status === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE'
  const action = newStatus === 'MAINTENANCE' ? '设为维护' : '上架'

  try {
    await ElMessageBox.confirm(`确定要${action}该船舶吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateShipStatusAPI(ship.id, newStatus)
    ElMessage.success(`船舶已${action}`)
    loadShips()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const terminateContract = async (contract) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入终止原因', '强制终止合约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入终止原因'
    })

    await forceTerminateContract(contract.id, reason)
    ElMessage.success('合约已终止')
    loadContracts()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const viewUserDetail = (user) => {
  ElMessageBox.alert(
    `
      <div style="text-align: left;">
        <p><strong>用户ID：</strong>${user.id}</p>
        <p><strong>用户名：</strong>${user.username}</p>
        <p><strong>邮箱：</strong>${user.email}</p>
        <p><strong>角色：</strong>${getRoleText(user.role)}</p>
        <p><strong>公司名称：</strong>${user.companyName || '未提供'}</p>
        <p><strong>信用评分：</strong>${user.creditScore}</p>
        <p><strong>状态：</strong>${getStatusText(user.status)}</p>
        <p><strong>注册时间：</strong>${user.createdAt || '未知'}</p>
      </div>
    `,
    '用户详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

onMounted(() => {
  loadStatistics()
  loadUsers()
  loadShips()
  loadContracts()
})
</script>

<style scoped>
.admin-dashboard {
  position: relative;
  min-height: 100vh;
  padding: 0;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 50%, #0d1428 100%);
  overflow-x: hidden;
}

/* 顶部导航栏 */
.admin-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(10, 14, 39, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 2px solid rgba(30, 144, 255, 0.3);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5), 0 0 40px rgba(30, 144, 255, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  color: #ffd700;
  animation: rotate 4s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.logo-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 2px;
}

.logo-text .subtitle {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(30, 144, 255, 0.1);
  border: 1px solid rgba(30, 144, 255, 0.3);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
}

.logout-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #ff4757 100%);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(245, 108, 108, 0.5);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin: 32px 40px;
  padding: 0;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
  background: rgba(15, 20, 40, 0.8);
  backdrop-filter: blur(10px);
  border: 2px solid transparent;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(30, 144, 255, 0.1) 0%, rgba(255, 215, 0, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  border-color: rgba(30, 144, 255, 0.5);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 40px rgba(30, 144, 255, 0.4),
    inset 0 0 20px rgba(30, 144, 255, 0.1);
}

.stat-icon {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.stat-icon::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 16px;
  padding: 2px;
  background: linear-gradient(135deg, rgba(30, 144, 255, 0.5), rgba(255, 215, 0, 0.5));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover .stat-icon::after {
  opacity: 1;
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-content h3 {
  margin: 0 0 8px 0;
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e90ff 0%, #ffd700 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(30, 144, 255, 0.5);
}

.stat-content p {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  letter-spacing: 1px;
}

.section {
  margin: 0 40px 32px 40px;
  padding: 32px;
  background: rgba(15, 20, 40, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(30, 144, 255, 0.2);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 0 40px rgba(30, 144, 255, 0.05);
  transition: all 0.3s ease;
}

.section:hover {
  border-color: rgba(30, 144, 255, 0.4);
  box-shadow:
    0 12px 48px rgba(0, 0, 0, 0.5),
    0 0 60px rgba(30, 144, 255, 0.2),
    inset 0 0 60px rgba(30, 144, 255, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(30, 144, 255, 0.2);
}

.section-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 0 20px rgba(30, 144, 255, 0.5);
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 24px;
  margin: 0 40px 40px 40px;
}

.chart-card {
  padding: 32px;
  background: rgba(15, 20, 40, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(30, 144, 255, 0.2);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 0 40px rgba(30, 144, 255, 0.05);
  transition: all 0.3s ease;
}

.chart-card:hover {
  border-color: rgba(30, 144, 255, 0.4);
  transform: translateY(-4px);
  box-shadow:
    0 12px 48px rgba(0, 0, 0, 0.5),
    0 0 40px rgba(30, 144, 255, 0.2);
}

.chart-card h3 {
  margin: 0 0 28px 0;
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e90ff 0%, #ffd700 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.chart-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chart-label {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 0.5px;
}

:deep(.maritime-dialog .el-dialog__header) {
  background: var(--maritime-gradient-primary);
  color: white;
  padding: 20px;
}

:deep(.maritime-dialog .el-dialog__title) {
  color: white;
  font-weight: 700;
}

:deep(.maritime-dialog .el-dialog__close) {
  color: white;
}

:deep(.el-table) {
  background: transparent;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-table th),
:deep(.el-table th.el-table-fixed-column--right) {
  background: rgba(30, 144, 255, 0.15) !important;
  color: #fff !important;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 1px;
  border-bottom: 2px solid rgba(30, 144, 255, 0.3);
}

:deep(.el-table td),
:deep(.el-table td.el-table-fixed-column--right) {
  border-bottom: 1px solid rgba(30, 144, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
  background: transparent !important;
}

:deep(.el-table tr) {
  background: transparent;
  transition: all 0.3s ease;
}

:deep(.el-table tr:hover > td) {
  background: rgba(30, 144, 255, 0.1) !important;
  box-shadow: inset 0 0 20px rgba(30, 144, 255, 0.2);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(30, 144, 255, 0.03);
}

:deep(.el-table__empty-text) {
  color: rgba(255, 255, 255, 0.5);
}
</style>
