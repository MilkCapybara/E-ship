<template>
  <div class="inbox-page">
    <!-- 背景效果 -->
    <div class="particles-bg"></div>
    <div class="scan-lines"></div>

    <div class="page-header tech-card">
      <div class="header-content">
        <div class="title-section">
          <div class="icon-wrapper">
            <el-icon class="header-icon"><Message /></el-icon>
          </div>
          <div>
            <h2 class="tech-gradient-text">合约信箱</h2>
            <p class="subtitle">待审核的租赁申请</p>
          </div>
        </div>
        <div class="badge-wrapper">
          <el-badge :value="pendingCount" :max="99" class="pending-badge">
            <div class="badge-icon-wrapper">
              <el-icon :size="36" class="badge-icon"><Message /></el-icon>
            </div>
          </el-badge>
        </div>
      </div>
    </div>

    <!-- 合约列表 -->
    <div class="contracts-list">
      <div
        v-for="contract in contracts"
        :key="contract.id"
        class="contract-card tech-card"
      >
        <div class="card-glow"></div>

        <div class="contract-header">
          <div class="contract-info">
            <h3 class="contract-title">{{ contract.shipName }}</h3>
            <div class="status-badge">
              <span class="status-dot"></span>
              <span>待审核</span>
            </div>
          </div>
          <div class="contract-amount">
            <span class="amount-label">合约总额</span>
            <span class="amount-value">¥{{ contract.totalAmount.toLocaleString() }}</span>
          </div>
        </div>

        <div class="contract-body">
          <div class="info-grid">
            <div class="info-item">
              <el-icon class="info-icon"><User /></el-icon>
              <div class="info-content">
                <span class="label">租家</span>
                <span class="value">{{ contract.renterName }}</span>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon"><Calendar /></el-icon>
              <div class="info-content">
                <span class="label">租赁日期</span>
                <span class="value">{{ contract.startDate }} 至 {{ contract.endDate }}</span>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon star-icon"><Star /></el-icon>
              <div class="info-content">
                <span class="label">信用评分</span>
                <span class="value credit-score">{{ contract.renterCreditScore }} 分</span>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon"><Money /></el-icon>
              <div class="info-content">
                <span class="label">日租金</span>
                <span class="value daily-rent">¥{{ contract.dailyRent.toLocaleString() }}</span>
              </div>
            </div>
          </div>

          <div class="contract-details">
            <div class="detail-section">
              <h4>租赁用途</h4>
              <p>{{ contract.purpose }}</p>
            </div>
            <div class="detail-section" v-if="contract.specialRequirements">
              <h4>特殊要求</h4>
              <p>{{ contract.specialRequirements }}</p>
            </div>
          </div>
        </div>

        <div class="contract-actions">
          <el-button class="approve-btn" @click="handleApprove(contract)">
            <el-icon><Check /></el-icon>
            <span>同意</span>
          </el-button>
          <el-button class="reject-btn" @click="handleReject(contract)">
            <el-icon><Close /></el-icon>
            <span>拒绝</span>
          </el-button>
          <el-button class="view-btn" @click="viewDetail(contract)">
            <el-icon><View /></el-icon>
            <span>查看详情</span>
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="contracts.length === 0 && !loading" class="empty-state tech-card">
      <el-icon class="empty-icon"><Message /></el-icon>
      <p class="empty-text">暂无待审核的合约</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadContracts"
        @size-change="loadContracts"
      />
    </div>

    <!-- 拒绝对话框 -->
    <el-dialog
      v-model="showRejectDialog"
      title="拒绝合约"
      width="500px"
      class="tech-dialog"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            class="reject-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRejectDialog = false" class="cancel-btn">取消</el-button>
        <el-button @click="confirmReject" :loading="submitting" class="confirm-reject-btn">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, User, Calendar, Star, Money, Check, Close, View } from '@element-plus/icons-vue'
import { getOwnerInbox, approveContract, rejectContract } from '@/api/contract'
import { useUserStore } from '@/stores/user'
import wsService from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const contracts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showRejectDialog = ref(false)
const selectedContract = ref(null)
const submitting = ref(false)

const rejectForm = reactive({
  rejectReason: ''
})

const pendingCount = computed(() => total.value)

const loadContracts = async () => {
  loading.value = true
  try {
    const res = await getOwnerInbox({
      ownerId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value
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

    if (action === 'NEW_CONTRACT') {
      // 收到新合约申请
      ElMessage.success('收到新的租赁申请！')
      loadContracts() // 刷新列表
    } else if (action === 'CANCELLED') {
      // 租家取消了合约
      ElMessage.info('租家取消了一个合约申请')
      loadContracts() // 刷新列表
    }
  }
}

const handleApprove = async (contract) => {
  try {
    await ElMessageBox.confirm('确定同意这个租赁申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await approveContract(contract.id, userStore.userInfo.id)
    ElMessage.success('合约已同意')
    loadContracts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReject = (contract) => {
  selectedContract.value = contract
  rejectForm.rejectReason = ''
  showRejectDialog.value = true
}

const confirmReject = async () => {
  if (!rejectForm.rejectReason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  submitting.value = true
  try {
    await rejectContract(selectedContract.value.id, userStore.userInfo.id, {
      approved: false,
      rejectReason: rejectForm.rejectReason
    })
    ElMessage.success('合约已拒绝')
    showRejectDialog.value = false
    loadContracts()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
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
  wsService.addListener('owner-inbox', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('owner-inbox')
})
</script>

<style scoped>
/* 页面容器 */
.inbox-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
  overflow-y: auto;
  max-height: calc(100vh - 60px);
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
  position: relative;
  z-index: 1;
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

.badge-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pending-badge {
  position: relative;
}

.badge-icon-wrapper {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 214, 92, 0.1);
  border: 2px solid rgba(255, 214, 92, 0.3);
  border-radius: 50%;
  animation: badge-pulse 2s ease-in-out infinite;
}

@keyframes badge-pulse {
  0%, 100% {
    box-shadow: 0 0 15px rgba(255, 214, 92, 0.3);
  }
  50% {
    box-shadow: 0 0 25px rgba(255, 214, 92, 0.6);
  }
}

.badge-icon {
  color: #ffd65c;
  filter: drop-shadow(0 0 8px rgba(255, 214, 92, 0.5));
}

/* 合约列表 */
.contracts-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
}

/* 合约卡片 */
.contract-card {
  padding: 0;
  overflow: hidden;
  transition: all 0.3s ease;
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
  transform: translateX(8px);
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

/* 合约头部 */
.contract-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 28px 28px 20px 28px;
  border-bottom: 2px solid rgba(79, 168, 255, 0.2);
  position: relative;
  z-index: 1;
}

.contract-info {
  flex: 1;
}

.contract-title {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
  letter-spacing: 0.5px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(230, 162, 60, 0.2);
  border: 1px solid rgba(230, 162, 60, 0.5);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
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

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e6a23c;
  box-shadow: 0 0 10px rgba(230, 162, 60, 0.6);
  animation: dot-pulse 2s ease-in-out infinite;
}

@keyframes dot-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
}

.contract-amount {
  text-align: right;
}

.amount-label {
  display: block;
  font-size: 13px;
  color: rgba(233, 246, 255, 0.7);
  margin-bottom: 6px;
}

.amount-value {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #ffd65c;
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

/* 合约主体 */
.contract-body {
  padding: 0 28px 28px 28px;
  position: relative;
  z-index: 1;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(79, 168, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(79, 168, 255, 0.1);
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(79, 168, 255, 0.1);
  border-color: rgba(79, 168, 255, 0.3);
  transform: translateX(5px);
}

.info-icon {
  color: #4fa8ff;
  font-size: 20px;
  filter: drop-shadow(0 0 5px rgba(79, 168, 255, 0.5));
}

.star-icon {
  color: #ffd65c;
  filter: drop-shadow(0 0 5px rgba(255, 214, 92, 0.5));
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.info-content .label {
  font-size: 12px;
  color: rgba(233, 246, 255, 0.7);
}

.info-content .value {
  font-size: 15px;
  font-weight: 600;
  color: #e9f6ff;
}

.credit-score {
  color: #ffd65c;
  font-weight: 700;
  text-shadow: 0 0 8px rgba(255, 214, 92, 0.4);
}

.daily-rent {
  color: #4fa8ff;
  font-weight: 700;
}

/* 合约详情 */
.contract-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.detail-section {
  padding: 18px;
  background: rgba(79, 168, 255, 0.05);
  border-radius: 10px;
  border-left: 4px solid #4fa8ff;
  transition: all 0.3s ease;
}

.detail-section:hover {
  background: rgba(79, 168, 255, 0.08);
  border-left-color: #ffd65c;
  transform: translateX(3px);
}

.detail-section h4 {
  margin: 0 0 10px 0;
  font-size: 15px;
  font-weight: 700;
  color: #4fa8ff;
  text-shadow: 0 0 8px rgba(79, 168, 255, 0.3);
}

.detail-section p {
  margin: 0;
  font-size: 14px;
  color: rgba(233, 246, 255, 0.85);
  line-height: 1.6;
}

/* 合约操作 */
.contract-actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
}

.contract-actions .el-button {
  flex: 1;
  position: relative;
  overflow: hidden;
  font-weight: 600;
  transition: all 0.3s ease;
}

.approve-btn {
  background: linear-gradient(135deg,
    rgba(103, 194, 58, 0.2),
    rgba(103, 194, 58, 0.3)) !important;
  border: 1px solid rgba(103, 194, 58, 0.5) !important;
  color: #67c23a !important;
}

.approve-btn:hover {
  background: linear-gradient(135deg,
    rgba(103, 194, 58, 0.3),
    rgba(103, 194, 58, 0.4)) !important;
  border-color: rgba(103, 194, 58, 0.8) !important;
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.4) !important;
  transform: translateY(-2px);
}

.reject-btn {
  background: linear-gradient(135deg,
    rgba(245, 108, 108, 0.2),
    rgba(245, 108, 108, 0.3)) !important;
  border: 1px solid rgba(245, 108, 108, 0.5) !important;
  color: #f56c6c !important;
}

.reject-btn:hover {
  background: linear-gradient(135deg,
    rgba(245, 108, 108, 0.3),
    rgba(245, 108, 108, 0.4)) !important;
  border-color: rgba(245, 108, 108, 0.8) !important;
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.4) !important;
  transform: translateY(-2px);
}

.view-btn {
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.2),
    rgba(79, 168, 255, 0.3)) !important;
  border: 1px solid rgba(79, 168, 255, 0.5) !important;
  color: #4fa8ff !important;
}

.view-btn:hover {
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.3),
    rgba(79, 168, 255, 0.4)) !important;
  border-color: rgba(79, 168, 255, 0.8) !important;
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.4) !important;
  transform: translateY(-2px);
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

/* 对话框样式 */
.tech-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.95) 0%,
    rgba(11, 31, 54, 0.95) 100%);
  border: 2px solid rgba(79, 168, 255, 0.4);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.8),
    0 0 80px rgba(79, 168, 255, 0.3);
}

.tech-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.95),
    rgba(255, 183, 3, 0.95));
  color: #051423;
  padding: 24px;
  border-bottom: 2px solid rgba(255, 214, 92, 0.5);
}

.tech-dialog :deep(.el-dialog__title) {
  color: #051423;
  font-weight: 800;
  font-size: 20px;
  letter-spacing: 1px;
}

.tech-dialog :deep(.el-dialog__close) {
  color: #051423;
  font-size: 20px;
  transition: all 0.3s ease;
}

.tech-dialog :deep(.el-dialog__close:hover) {
  color: #061627;
  transform: rotate(90deg) scale(1.1);
}

.tech-dialog :deep(.el-dialog__body) {
  padding: 24px;
  color: #e9f6ff;
}

.tech-dialog :deep(.el-form-item__label) {
  color: #ffd65c;
  font-weight: 600;
}

.tech-dialog :deep(.el-textarea__inner) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #e9f6ff;
  transition: all 0.3s ease;
}

.tech-dialog :deep(.el-textarea__inner:focus) {
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.2);
}

.cancel-btn {
  background: rgba(15, 43, 75, 0.6) !important;
  border: 1px solid rgba(79, 168, 255, 0.3) !important;
  color: #e9f6ff !important;
}

.cancel-btn:hover {
  border-color: rgba(79, 168, 255, 0.5) !important;
  box-shadow: 0 0 15px rgba(79, 168, 255, 0.2) !important;
}

.confirm-reject-btn {
  background: linear-gradient(135deg,
    rgba(245, 108, 108, 0.9),
    rgba(245, 108, 108, 0.95)) !important;
  border: none !important;
  color: white !important;
  font-weight: 700;
}

.confirm-reject-btn:hover {
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.5) !important;
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .contract-details {
    grid-template-columns: 1fr;
  }

  .contract-actions {
    flex-direction: column;
  }
}

/* 滚动条样式 */
.inbox-page::-webkit-scrollbar {
  width: 8px;
}

.inbox-page::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.inbox-page::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.6),
    rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

.inbox-page::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.8),
    rgba(79, 168, 255, 0.8));
}
</style>
