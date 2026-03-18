<template>
  <div class="contracts-page">
    <!-- 背景效果 -->
    <div class="particles-bg"></div>
    <div class="scan-lines"></div>

    <div class="page-header tech-card">
      <div class="header-content">
        <div class="title-section">
          <div class="icon-wrapper">
            <el-icon class="header-icon"><Document /></el-icon>
          </div>
          <div>
            <h2 class="tech-gradient-text">合约管理</h2>
            <p class="subtitle">查看和管理您的所有合约</p>
          </div>
        </div>
        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-label">总合约</span>
            <span class="stat-value">{{ total }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar tech-card">
      <el-select
        v-model="filterStatus"
        placeholder="合约状态"
        class="status-select"
        clearable
      >
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-button @click="loadContracts" class="search-btn">
        <el-icon><Search /></el-icon>
        <span>搜索</span>
      </el-button>
    </div>

    <!-- 合约列表 -->
    <div class="contracts-grid">
      <div
        v-for="contract in contracts"
        :key="contract.id"
        class="contract-card tech-card"
        @click="viewDetail(contract)"
      >
        <div class="card-glow"></div>
        <div class="contract-status" :class="`status-${contract.status.toLowerCase()}`">
          {{ getStatusText(contract.status) }}
        </div>

        <div class="contract-content">
          <h3 class="contract-title">{{ contract.shipName }}</h3>

          <div class="contract-info">
            <div class="info-row">
              <el-icon class="info-icon"><User /></el-icon>
              <span class="info-label">租家：</span>
              <span class="info-value">{{ contract.renterName }}</span>
            </div>
            <div class="info-row">
              <el-icon class="info-icon"><Calendar /></el-icon>
              <span class="info-label">租期：</span>
              <span class="info-value">{{ contract.startDate }} 至 {{ contract.endDate }}</span>
            </div>
            <div class="info-row">
              <el-icon class="info-icon"><Money /></el-icon>
              <span class="info-label">总额：</span>
              <span class="info-value amount">¥{{ contract.totalAmount.toLocaleString() }}</span>
            </div>
          </div>

          <div class="contract-footer">
            <el-button class="detail-btn" @click.stop="viewDetail(contract)">
              <span>查看详情</span>
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="contracts.length === 0 && !loading" class="empty-state tech-card">
      <el-icon class="empty-icon"><Document /></el-icon>
      <p class="empty-text">暂无合约数据</p>
    </div>

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
import { ElMessage } from 'element-plus'
import { Search, User, Calendar, Money, Document, ArrowRight } from '@element-plus/icons-vue'
import { getMyContracts } from '@/api/contract'
import { useUserStore } from '@/stores/user'
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
    const res = await getMyContracts({
      ownerId: userStore.userInfo.id,
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

const viewDetail = (contract) => {
  router.push(`/contract/${contract.id}`)
}

// WebSocket消息处理
const handleWebSocketMessage = (notification) => {
  if (notification.type === 'CONTRACT_UPDATE') {
    const { action } = notification.data

    if (action === 'NEW_CONTRACT') {
      ElMessage.success('收到新的租赁申请！')
      loadContracts() // 刷新列表
    } else if (action === 'CANCELLED') {
      ElMessage.info('租家取消了一个合约申请')
      loadContracts() // 刷新列表
    }
  }
}

onMounted(() => {
  loadContracts()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('owner-contracts', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('owner-contracts')
})
</script>

<style scoped>
/* 页面容器 */
.contracts-page {
  position: relative;
  min-height: calc(100vh - 60px);
  max-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
  overflow-y: auto;
}

/* 背景粒子效果 */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(2px 2px at 20% 30%, rgba(255, 214, 92, 0.3), transparent),
    radial-gradient(2px 2px at 60% 70%, rgba(79, 168, 255, 0.3), transparent),
    radial-gradient(1px 1px at 50% 50%, rgba(255, 214, 92, 0.2), transparent),
    radial-gradient(1px 1px at 80% 10%, rgba(79, 168, 255, 0.2), transparent);
  background-size: 200% 200%;
  animation: particles-float 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes particles-float {
  0%, 100% {
    background-position: 0% 0%;
    opacity: 0.6;
  }
  50% {
    background-position: 100% 100%;
    opacity: 1;
  }
}

/* 扫描线效果 */
.scan-lines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    0deg,
    rgba(255, 214, 92, 0.03) 0px,
    transparent 1px,
    transparent 2px,
    rgba(255, 214, 92, 0.03) 3px
  );
  pointer-events: none;
  z-index: 0;
  animation: scan-move 8s linear infinite;
}

@keyframes scan-move {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(10px);
  }
}

/* 科技卡片基础样式 */
.tech-card {
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
  z-index: 1;
  transition: all 0.3s ease;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  padding: 28px;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(255, 183, 3, 0.2));
  border: 2px solid rgba(255, 214, 92, 0.4);
  border-radius: 16px;
  animation: icon-pulse 3s ease-in-out infinite;
}

@keyframes icon-pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 214, 92, 0.4);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 214, 92, 0.6);
    transform: scale(1.05);
  }
}

.header-icon {
  font-size: 32px;
  color: #ffd65c;
  filter: drop-shadow(0 0 10px rgba(255, 214, 92, 0.6));
}

.tech-gradient-text {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffd65c 0%, #ffb703 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(255, 214, 92, 0.3);
  letter-spacing: 1px;
}

.subtitle {
  color: rgba(233, 246, 255, 0.7);
  margin: 0;
  font-size: 15px;
}

.stats-section {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 12px;
}

.stat-label {
  color: rgba(233, 246, 255, 0.7);
  font-size: 13px;
}

.stat-value {
  color: #ffd65c;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
  animation: value-glow 2s ease-in-out infinite;
}

@keyframes value-glow {
  0%, 100% {
    text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
  }
  50% {
    text-shadow: 0 0 25px rgba(255, 214, 92, 0.8);
  }
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 20px;
  margin-bottom: 24px;
  align-items: center;
}

:deep(.status-select) {
  width: 220px;
}

:deep(.status-select .el-input__wrapper) {
  background: rgba(79, 168, 255, 0.1) !important;
  border: 1px solid rgba(79, 168, 255, 0.3) !important;
  box-shadow: none !important;
  transition: all 0.3s ease !important;
}

:deep(.status-select .el-input__wrapper:hover) {
  border-color: rgba(255, 214, 92, 0.5) !important;
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.2) !important;
}

:deep(.status-select .el-input__inner) {
  color: #e9f6ff !important;
}

:deep(.status-select .el-input__inner::placeholder) {
  color: rgba(233, 246, 255, 0.5) !important;
}

.search-btn {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(255, 183, 3, 0.2)) !important;
  border: 1px solid rgba(255, 214, 92, 0.5) !important;
  color: #ffd65c !important;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.search-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 214, 92, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.search-btn:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.3),
    rgba(255, 183, 3, 0.3)) !important;
  border-color: rgba(255, 214, 92, 0.8) !important;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.4) !important;
  transform: translateY(-2px);
}

.search-btn:hover::before {
  width: 300px;
  height: 300px;
}

/* 合约网格 */
.contracts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* 合约卡片 */
.contract-card {
  cursor: pointer;
  overflow: hidden;
  padding: 0;
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
    0 0 30px rgba(255, 214, 92, 0.3),
    inset 0 1px 0 rgba(255, 214, 92, 0.2);
}

.card-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(79, 168, 255, 0.2));
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
  filter: blur(10px);
}

.contract-card:hover .card-glow {
  opacity: 1;
}

/* 合约状态标签 */
.contract-status {
  position: absolute;
  top: 20px;
  right: 20px;
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
  padding: 28px;
  position: relative;
  z-index: 1;
}

.contract-title {
  margin: 0 0 20px 0;
  font-size: 22px;
  font-weight: 700;
  color: #ffd65c;
  padding-right: 100px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
  letter-spacing: 0.5px;
}

/* 合约信息 */
.contract-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  padding: 10px;
  background: rgba(79, 168, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(79, 168, 255, 0.1);
  transition: all 0.3s ease;
}

.info-row:hover {
  background: rgba(79, 168, 255, 0.1);
  border-color: rgba(79, 168, 255, 0.3);
  transform: translateX(5px);
}

.info-icon {
  color: #4fa8ff;
  font-size: 18px;
  filter: drop-shadow(0 0 5px rgba(79, 168, 255, 0.5));
}

.info-label {
  color: rgba(233, 246, 255, 0.7);
  min-width: 50px;
}

.info-value {
  color: #e9f6ff;
  font-weight: 500;
  flex: 1;
}

.info-value.amount {
  color: #ffd65c;
  font-weight: 700;
  font-size: 16px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

/* 合约底部 */
.contract-footer {
  padding-top: 20px;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
  display: flex;
  justify-content: flex-end;
}

.detail-btn {
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.2),
    rgba(79, 168, 255, 0.3)) !important;
  border: 1px solid rgba(79, 168, 255, 0.5) !important;
  color: #4fa8ff !important;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-btn:hover {
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.3),
    rgba(79, 168, 255, 0.4)) !important;
  border-color: rgba(79, 168, 255, 0.8) !important;
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.4) !important;
  transform: translateX(5px);
}

/* 空状态 */
.empty-state {
  padding: 60px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.empty-icon {
  font-size: 80px;
  color: rgba(79, 168, 255, 0.3);
  animation: empty-pulse 3s ease-in-out infinite;
}

@keyframes empty-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.05);
  }
}

.empty-text {
  color: rgba(233, 246, 255, 0.6);
  font-size: 16px;
  margin: 0;
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

:deep(.el-pagination) {
  --el-pagination-bg-color: rgba(79, 168, 255, 0.1);
  --el-pagination-text-color: #e9f6ff;
  --el-pagination-hover-color: #ffd65c;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .contracts-grid {
    grid-template-columns: 1fr;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-section {
    width: 100%;
    justify-content: space-around;
  }

  .filter-bar {
    flex-direction: column;
  }

  :deep(.status-select) {
    width: 100%;
  }
}
</style>
