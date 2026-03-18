<template>
  <div class="contracts-page">
    <ParticleBackground />

    <div class="page-header maritime-card">
      <div class="header-content">
        <div>
          <h2 class="maritime-gradient-text">我的合约</h2>
          <p class="subtitle">查看您的租赁合约</p>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar maritime-card">
      <MaritimeSelect
        v-model="filterStatus"
        :options="statusOptions"
        placeholder="合约状态"
        style="width: 200px"
      />
      <el-button @click="loadContracts">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 合约列表 -->
    <div class="contracts-grid">
      <div
        v-for="contract in contracts"
        :key="contract.id"
        class="contract-card maritime-card"
        @click="viewDetail(contract)"
      >
        <div class="contract-status" :class="`status-${contract.status.toLowerCase()}`">
          {{ getStatusText(contract.status) }}
        </div>

        <div class="contract-content">
          <h3>{{ contract.shipName }}</h3>

          <div class="contract-info">
            <div class="info-row">
              <el-icon><Calendar /></el-icon>
              <span>{{ contract.startDate }} 至 {{ contract.endDate }}</span>
            </div>
            <div class="info-row">
              <el-icon><Money /></el-icon>
              <span>总额：¥{{ contract.totalAmount.toLocaleString() }}</span>
            </div>
          </div>

          <div class="contract-footer">
            <el-button
              v-if="contract.status === 'PENDING'"
              size="small"
              type="danger"
              @click.stop="handleCancel(contract)"
            >
              取消申请
            </el-button>
            <el-button size="small" @click.stop="viewDetail(contract)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="contracts.length === 0 && !loading" description="暂无合约数据" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadContracts"
        @size-change="loadContracts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Calendar, Money } from '@element-plus/icons-vue'
import { getRenterContracts, cancelContract } from '@/api/contract'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeSelect from '@/components/MaritimeSelect.vue'
import wsService from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const contracts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const filterStatus = ref('')

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已同意', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const getStatusText = (status) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option ? option.label : status
}

const loadContracts = async () => {
  loading.value = true
  try {
    const res = await getRenterContracts({
      renterId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      status: filterStatus.value || undefined
    })
    contracts.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载合约列表失败')
  } finally {
    loading.value = false
  }
}

// WebSocket消息处理
const handleWebSocketMessage = (notification) => {
  if (notification.type === 'CONTRACT_UPDATE') {
    const { action, contractId } = notification.data

    if (action === 'APPROVED') {
      // 船东同意了合约
      ElMessage.success('您的租赁申请已被同意！')
      loadContracts() // 刷新列表
    } else if (action === 'REJECTED') {
      // 船东拒绝了合约
      ElMessage.warning('您的租赁申请被拒绝了')
      loadContracts() // 刷新列表
    }
  }
}

const handleCancel = async (contract) => {
  try {
    await ElMessageBox.confirm('确定要取消这个租赁申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelContract(contract.id, userStore.userInfo.id)
    ElMessage.success('申请已取消')
    loadContracts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const viewDetail = (contract) => {
  router.push(`/contract/${contract.id}`)
}

onMounted(() => {
  loadContracts()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('renter-contracts', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('renter-contracts')
})
</script>

<style scoped>
/* 页面容器 */
.contracts-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
  overflow-y: auto;
  max-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  padding: 28px;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 214, 92, 0.1);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 214, 92, 0.1) 50%,
    transparent 100%);
  animation: header-shine 6s ease-in-out infinite;
}

@keyframes header-shine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-content h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #ffd65c 0%, #ffb703 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(255, 214, 92, 0.3);
}

.subtitle {
  color: rgba(233, 246, 255, 0.7);
  margin: 0;
  font-size: 15px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 20px;
  margin-bottom: 24px;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 100;
}

/* 合约网格 */
.contracts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

/* 合约卡片 */
.contract-card {
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.contract-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 214, 92, 0.1) 50%,
    transparent 100%);
  transition: left 0.6s ease;
  z-index: 0;
}

.contract-card:hover::before {
  left: 100%;
}

.contract-card:hover {
  transform: translateY(-8px);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(255, 214, 92, 0.3);
}

/* 合约状态标签 */
.contract-status {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  z-index: 2;
  backdrop-filter: blur(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  animation: status-float 3s ease-in-out infinite;
}

@keyframes status-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.status-pending {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.9), rgba(230, 162, 60, 0.7));
  border: 1px solid rgba(230, 162, 60, 0.5);
}

.status-approved {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.9), rgba(103, 194, 58, 0.7));
  border: 1px solid rgba(103, 194, 58, 0.5);
}

.status-rejected {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.9), rgba(245, 108, 108, 0.7));
  border: 1px solid rgba(245, 108, 108, 0.5);
}

.status-in_progress {
  background: linear-gradient(135deg, rgba(79, 168, 255, 0.9), rgba(79, 168, 255, 0.7));
  border: 1px solid rgba(79, 168, 255, 0.5);
}

.status-completed {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.9), rgba(144, 147, 153, 0.7));
  border: 1px solid rgba(144, 147, 153, 0.5);
}

.status-cancelled {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.9), rgba(144, 147, 153, 0.7));
  border: 1px solid rgba(144, 147, 153, 0.5);
}

/* 合约内容 */
.contract-content {
  padding: 24px;
  position: relative;
  z-index: 1;
}

.contract-content h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffd65c;
  padding-right: 80px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
}

/* 合约信息 */
.contract-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(233, 246, 255, 0.85);
  padding: 8px;
  background: rgba(79, 168, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(79, 168, 255, 0.1);
  transition: all 0.3s ease;
}

.info-row:hover {
  background: rgba(79, 168, 255, 0.1);
  border-color: rgba(79, 168, 255, 0.3);
  transform: translateX(3px);
}

.info-row .el-icon {
  color: #4fa8ff;
  filter: drop-shadow(0 0 5px rgba(79, 168, 255, 0.5));
}

/* 合约底部 */
.contract-footer {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
}

.contract-footer .el-button {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #e9f6ff;
  transition: all 0.3s ease;
}

.contract-footer .el-button:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(255, 183, 3, 0.2));
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.3);
  transform: translateY(-2px);
}

.contract-footer .el-button--danger {
  background: rgba(245, 108, 108, 0.1);
  border-color: rgba(245, 108, 108, 0.3);
  color: #f56c6c;
}

.contract-footer .el-button--danger:hover {
  background: linear-gradient(135deg,
    rgba(245, 108, 108, 0.2),
    rgba(245, 108, 108, 0.3));
  border-color: rgba(245, 108, 108, 0.5);
  box-shadow: 0 0 15px rgba(245, 108, 108, 0.3);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

:deep(.el-pagination button) {
  background: rgba(79, 168, 255, 0.1) !important;
  color: #e9f6ff !important;
  border: 1px solid rgba(79, 168, 255, 0.3) !important;
}

:deep(.el-pagination button:hover) {
  color: #ffd65c !important;
  border-color: rgba(255, 214, 92, 0.5) !important;
}

:deep(.el-pager li) {
  background: rgba(79, 168, 255, 0.1) !important;
  color: #e9f6ff !important;
  border: 1px solid rgba(79, 168, 255, 0.3) !important;
  margin: 0 4px;
  border-radius: 8px;
}

:deep(.el-pager li:hover) {
  color: #ffd65c !important;
  border-color: rgba(255, 214, 92, 0.5) !important;
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.3),
    rgba(255, 183, 3, 0.3)) !important;
  color: #ffd65c !important;
  border-color: rgba(255, 214, 92, 0.6) !important;
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.3);
}

/* 下拉菜单样式 */
:deep(.el-select-dropdown) {
  z-index: 9999 !important;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.98) 0%,
    rgba(11, 31, 54, 0.98) 100%);
  border: 1px solid rgba(79, 168, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5), 0 0 40px rgba(79, 168, 255, 0.2);
  backdrop-filter: blur(20px);
}

:deep(.el-select-dropdown__item) {
  color: #e9f6ff;
  transition: all 0.3s ease;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(79, 168, 255, 0.2);
  color: #ffd65c;
}

:deep(.el-select-dropdown__item.is-selected) {
  background: rgba(255, 214, 92, 0.2);
  color: #ffd65c;
  font-weight: 700;
}

/* 滚动条样式 */
.contracts-page::-webkit-scrollbar {
  width: 8px;
}

.contracts-page::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.contracts-page::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.6),
    rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

.contracts-page::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.8),
    rgba(79, 168, 255, 0.8));
}
</style>
