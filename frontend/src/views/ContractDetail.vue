<template>
  <div class="contract-detail-page">
    <ParticleBackground />

    <div class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-button-wrapper">
        <el-button @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <!-- 合约头部 -->
      <div class="contract-header maritime-card">
        <div class="header-left">
          <h1 class="maritime-gradient-text">合约详情</h1>
          <div class="contract-id">合约编号：#{{ contract.id }}</div>
        </div>
        <div class="header-right">
          <el-tag :type="getStatusTagType(contract.status)" size="large">
            {{ getStatusText(contract.status) }}
          </el-tag>
        </div>
      </div>

      <!-- 合约主要信息 -->
      <div class="info-grid">
        <!-- 船舶信息 -->
        <div class="info-card maritime-card">
          <h3 class="card-title">
            <el-icon><Ship /></el-icon>
            船舶信息
          </h3>
          <div class="info-list">
            <div class="info-item">
              <span class="label">船舶名称</span>
              <span class="value">{{ contract.shipName }}</span>
            </div>
            <div class="info-item">
              <span class="label">船舶类型</span>
              <span class="value">{{ getShipTypeText(contract.shipType) }}</span>
            </div>
            <div class="info-item">
              <span class="label">日租金</span>
              <span class="value price">¥{{ contract.dailyRent?.toLocaleString() }}</span>
            </div>
          </div>
        </div>

        <!-- 租赁方信息 -->
        <div class="info-card maritime-card">
          <h3 class="card-title">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo.role === 'SHIP_OWNER' ? '租家信息' : '船东信息' }}
          </h3>
          <div class="info-list">
            <div class="info-item">
              <span class="label">{{ userStore.userInfo.role === 'SHIP_OWNER' ? '租家' : '船东' }}</span>
              <span class="value">{{ userStore.userInfo.role === 'SHIP_OWNER' ? contract.renterName : contract.ownerName }}</span>
            </div>
            <div class="info-item">
              <span class="label">公司名称</span>
              <span class="value">{{ userStore.userInfo.role === 'SHIP_OWNER' ? contract.renterCompany : contract.ownerCompany || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">信用评分</span>
              <el-progress
                :percentage="userStore.userInfo.role === 'SHIP_OWNER' ? contract.renterCreditScore : contract.ownerCreditScore || 100"
                :color="getCreditColor(userStore.userInfo.role === 'SHIP_OWNER' ? contract.renterCreditScore : contract.ownerCreditScore || 100)"
                :stroke-width="12"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 租赁详情 -->
      <div class="detail-card maritime-card">
        <h3 class="card-title">
          <el-icon><Calendar /></el-icon>
          租赁详情
        </h3>
        <div class="detail-grid">
          <div class="detail-item">
            <div class="detail-label">开始日期</div>
            <div class="detail-value">{{ contract.startDate }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">结束日期</div>
            <div class="detail-value">{{ contract.endDate }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">租赁天数</div>
            <div class="detail-value">{{ calculateDays() }} 天</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">合约总额</div>
            <div class="detail-value price-large">¥{{ contract.totalAmount?.toLocaleString() }}</div>
          </div>
        </div>
      </div>

      <!-- 租赁用途和要求 -->
      <div class="purpose-card maritime-card">
        <h3 class="card-title">
          <el-icon><Document /></el-icon>
          租赁用途
        </h3>
        <p class="purpose-text">{{ contract.purpose || '未提供' }}</p>

        <h3 class="card-title" v-if="contract.specialRequirements">
          <el-icon><Warning /></el-icon>
          特殊要求
        </h3>
        <p class="purpose-text" v-if="contract.specialRequirements">
          {{ contract.specialRequirements }}
        </p>
      </div>

      <!-- 审核信息 -->
      <div class="review-card maritime-card" v-if="contract.status === 'REJECTED' && contract.rejectReason">
        <h3 class="card-title reject-title">
          <el-icon><Close /></el-icon>
          拒绝原因
        </h3>
        <p class="reject-text">{{ contract.rejectReason }}</p>
      </div>

      <!-- 操作按钮 -->
      <div class="action-card maritime-card">
        <div class="action-buttons">
          <!-- 船东操作 -->
          <template v-if="userStore.userInfo.role === 'SHIP_OWNER' && contract.status === 'PENDING'">
            <el-button type="success" size="large" @click="handleApprove">
              <el-icon><Check /></el-icon>
              同意合约
            </el-button>
            <el-button type="danger" size="large" @click="handleReject">
              <el-icon><Close /></el-icon>
              拒绝合约
            </el-button>
          </template>

          <!-- 租家操作 -->
          <template v-if="userStore.userInfo.role === 'RENTER' && contract.status === 'PENDING'">
            <el-button type="danger" size="large" @click="handleCancel">
              <el-icon><Close /></el-icon>
              取消申请
            </el-button>
          </template>

          <!-- 管理员操作 -->
          <template v-if="userStore.userInfo.role === 'ADMIN' && (contract.status === 'IN_PROGRESS' || contract.status === 'APPROVED')">
            <el-button type="danger" size="large" @click="handleTerminate">
              <el-icon><Close /></el-icon>
              强制终止
            </el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 拒绝对话框 -->
    <el-dialog
      v-model="showRejectDialog"
      title="拒绝合约"
      width="500px"
      class="maritime-dialog"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="submitting">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Ship, User, Calendar, Document, Warning, Close, Check } from '@element-plus/icons-vue'
import { getContractDetail, approveContract, rejectContract, cancelContract } from '@/api/contract'
import { forceTerminateContract } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const contract = ref({})
const showRejectDialog = ref(false)
const submitting = ref(false)

const rejectForm = reactive({
  rejectReason: ''
})

const shipTypeOptions = [
  { label: '集装箱船', value: 'CONTAINER' },
  { label: '散货船', value: 'BULK' },
  { label: '油船', value: 'TANKER' },
  { label: '客船', value: 'PASSENGER' }
]

const getShipTypeText = (type) => {
  const option = shipTypeOptions.find(opt => opt.value === type)
  return option ? option.label : type
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
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

const calculateDays = () => {
  if (!contract.value.startDate || !contract.value.endDate) return 0
  const start = new Date(contract.value.startDate)
  const end = new Date(contract.value.endDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
}

const loadContractDetail = async () => {
  try {
    const res = await getContractDetail(route.params.id)
    contract.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载合约详情失败')
  }
}

const handleApprove = async () => {
  try {
    await ElMessageBox.confirm('确定同意这个租赁申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await approveContract(contract.value.id, userStore.userInfo.id)
    ElMessage.success('合约已同意')
    loadContractDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReject = () => {
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
    await rejectContract(contract.value.id, userStore.userInfo.id, {
      rejectReason: rejectForm.rejectReason
    })
    ElMessage.success('合约已拒绝')
    showRejectDialog.value = false
    loadContractDetail()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个租赁申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelContract(contract.value.id, userStore.userInfo.id)
    ElMessage.success('申请已取消')
    loadContractDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleTerminate = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入终止原因', '强制终止合约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入终止原因'
    })

    await forceTerminateContract(contract.value.id, reason)
    ElMessage.success('合约已终止')
    loadContractDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadContractDetail()
})
</script>

<style scoped>
.contract-detail-page {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #0a1929 0%, #1a2332 50%, #0d1b2a 100%);
  overflow: hidden;
}

.contract-detail-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 30%, rgba(60, 235, 220, 0.08), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(79, 168, 255, 0.08), transparent 50%);
  pointer-events: none;
  animation: bg-pulse 8s ease-in-out infinite;
}

@keyframes bg-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.back-button-wrapper {
  margin-bottom: 24px;
}

.back-button {
  background: rgba(15, 43, 75, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #3cebdc;
  transition: all 0.3s;
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.1);
}

.back-button:hover {
  background: rgba(15, 43, 75, 0.95);
  border-color: rgba(60, 235, 220, 0.6);
  transform: translateX(-5px);
  box-shadow: 0 0 30px rgba(60, 235, 220, 0.3);
  color: #3cebdc;
}

.contract-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  margin-bottom: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(60, 235, 220, 0.1),
    inset 0 0 60px rgba(60, 235, 220, 0.03);
  position: relative;
  overflow: hidden;
}

.contract-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.1), transparent);
  animation: card-shine 3s ease-in-out infinite;
}

@keyframes card-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #3cebdc 0%, #4fa8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(60, 235, 220, 0.3);
}

.contract-id {
  font-size: 14px;
  color: rgba(233, 246, 255, 0.6);
  letter-spacing: 1px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.info-card {
  padding: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(60, 235, 220, 0.1),
    inset 0 0 60px rgba(60, 235, 220, 0.03);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.info-card:hover {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 50px rgba(60, 235, 220, 0.2),
    inset 0 0 80px rgba(60, 235, 220, 0.05);
  transform: translateY(-2px);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.card-title .el-icon {
  color: #3cebdc;
  filter: drop-shadow(0 0 8px rgba(60, 235, 220, 0.6));
}

.reject-title .el-icon {
  color: #ff5a7a;
  filter: drop-shadow(0 0 8px rgba(255, 90, 122, 0.6));
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(10, 14, 39, 0.5);
  border-radius: 10px;
  border: 1px solid rgba(79, 168, 255, 0.2);
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(10, 14, 39, 0.7);
  border-color: rgba(60, 235, 220, 0.4);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.1);
}

.info-item .label {
  font-size: 14px;
  color: rgba(233, 246, 255, 0.6);
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #e9f6ff;
}

.info-item .value.price {
  color: #3cebdc;
  text-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.detail-card {
  padding: 24px;
  margin-bottom: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(60, 235, 220, 0.1),
    inset 0 0 60px rgba(60, 235, 220, 0.03);
  position: relative;
  overflow: hidden;
}

.detail-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.detail-item {
  padding: 16px;
  background: rgba(10, 14, 39, 0.5);
  border-radius: 10px;
  border-left: 4px solid #3cebdc;
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: rgba(10, 14, 39, 0.7);
  box-shadow: 0 0 30px rgba(60, 235, 220, 0.2);
  transform: translateX(5px);
}

.detail-label {
  font-size: 12px;
  color: rgba(233, 246, 255, 0.6);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 18px;
  font-weight: 600;
  color: #e9f6ff;
}

.detail-value.price-large {
  font-size: 28px;
  color: #3cebdc;
  text-shadow: 0 0 20px rgba(60, 235, 220, 0.5);
}

.purpose-card {
  padding: 24px;
  margin-bottom: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(60, 235, 220, 0.1),
    inset 0 0 60px rgba(60, 235, 220, 0.03);
  position: relative;
  overflow: hidden;
}

.purpose-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.purpose-text {
  margin: 0 0 24px 0;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(233, 246, 255, 0.8);
  padding: 16px;
  background: rgba(10, 14, 39, 0.5);
  border-radius: 10px;
  border: 1px solid rgba(79, 168, 255, 0.2);
}

.review-card {
  padding: 24px;
  margin-bottom: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 2px solid rgba(255, 90, 122, 0.5);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(255, 90, 122, 0.2),
    inset 0 0 60px rgba(255, 90, 122, 0.05);
  position: relative;
  overflow: hidden;
}

.review-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255, 90, 122, 0.8), transparent);
  box-shadow: 0 0 10px rgba(255, 90, 122, 0.5);
}

.reject-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #ff5a7a;
  padding: 16px;
  background: rgba(255, 90, 122, 0.1);
  border-radius: 10px;
  border: 1px solid rgba(255, 90, 122, 0.3);
}

.action-card {
  padding: 24px;
  background: rgba(15, 43, 75, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(60, 235, 220, 0.1),
    inset 0 0 60px rgba(60, 235, 220, 0.03);
  position: relative;
  overflow: hidden;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 150px;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-buttons .el-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.action-buttons .el-button:hover::before {
  width: 300px;
  height: 300px;
}

.action-buttons .el-button--success {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.9), rgba(79, 168, 255, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(60, 235, 220, 0.3);
}

.action-buttons .el-button--success:hover {
  background: linear-gradient(135deg, rgba(60, 235, 220, 1), rgba(79, 168, 255, 1));
  box-shadow: 0 6px 25px rgba(60, 235, 220, 0.5);
  transform: translateY(-2px);
}

.action-buttons .el-button--danger {
  background: linear-gradient(135deg, rgba(255, 90, 122, 0.9), rgba(255, 60, 100, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 90, 122, 0.3);
}

.action-buttons .el-button--danger:hover {
  background: linear-gradient(135deg, rgba(255, 90, 122, 1), rgba(255, 60, 100, 1));
  box-shadow: 0 6px 25px rgba(255, 90, 122, 0.5);
  transform: translateY(-2px);
}

/* 拒绝对话框 - 毛玻璃航运金融科技风 */
:deep(.maritime-dialog) {
  z-index: 3000;
}

:deep(.maritime-dialog .el-dialog) {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.8),
    0 0 80px rgba(60, 235, 220, 0.3),
    inset 0 0 100px rgba(60, 235, 220, 0.05) !important;
  border: 2px solid rgba(79, 168, 255, 0.4) !important;
  border-radius: 20px !important;
  overflow: hidden;
  position: relative;
}

:deep(.maritime-dialog .el-dialog::before) {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(45deg,
    transparent 0%,
    rgba(60, 235, 220, 0.3) 25%,
    rgba(79, 168, 255, 0.3) 50%,
    rgba(255, 214, 92, 0.3) 75%,
    transparent 100%);
  border-radius: 20px;
  opacity: 0.5;
  z-index: -1;
  animation: dialog-border-glow 4s linear infinite;
  background-size: 400% 400%;
  pointer-events: none;
}

@keyframes dialog-border-glow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

:deep(.maritime-dialog .el-dialog__header) {
  position: relative;
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.95), rgba(79, 168, 255, 0.95)) !important;
  color: white;
  padding: 28px 32px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

:deep(.maritime-dialog .el-dialog__header::after) {
  content: '';
  position: absolute;
  right: -50px;
  top: 50%;
  transform: translateY(-50%);
  width: 150px;
  height: 150px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 150 150"><circle cx="75" cy="75" r="60" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="8,6"/><circle cx="75" cy="75" r="45" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="2"/><circle cx="75" cy="75" r="30" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="1.5" stroke-dasharray="4,4"/><path d="M50,75 L60,70 L70,73 L80,67 L90,70 L100,65" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="2"/></svg>') no-repeat center;
  background-size: contain;
  pointer-events: none;
  opacity: 0.3;
  animation: header-decoration-rotate 20s linear infinite;
}

@keyframes header-decoration-rotate {
  from { transform: translateY(-50%) rotate(0deg); }
  to { transform: translateY(-50%) rotate(360deg); }
}

:deep(.maritime-dialog .el-dialog__header::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: header-shine 3s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

@keyframes header-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

:deep(.maritime-dialog .el-dialog__title) {
  color: white;
  font-weight: 800;
  font-size: 24px;
  letter-spacing: 3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(255, 255, 255, 0.3),
    0 0 40px rgba(60, 235, 220, 0.2);
  position: relative;
  z-index: 2;
  text-transform: uppercase;
}

:deep(.maritime-dialog .el-dialog__close) {
  color: white;
  font-size: 22px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.maritime-dialog .el-dialog__close:hover) {
  color: #ffd65c;
  transform: rotate(90deg) scale(1.1);
  filter: drop-shadow(0 0 15px rgba(255, 214, 92, 0.8));
  background: rgba(255, 214, 92, 0.2);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
}

:deep(.maritime-dialog .el-dialog__body) {
  position: relative;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  color: #e9f6ff;
  overflow: hidden;
  padding: 32px 24px;
}

:deep(.maritime-dialog .el-dialog__body::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 30%, rgba(60, 235, 220, 0.1), transparent 50%),
    radial-gradient(circle at 70% 70%, rgba(79, 168, 255, 0.1), transparent 50%);
  pointer-events: none;
  animation: dialog-glow 4s ease-in-out infinite;
}

@keyframes dialog-glow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

:deep(.maritime-dialog .el-dialog__body::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

:deep(.maritime-dialog .el-dialog__footer) {
  position: relative;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.maritime-dialog .el-dialog__footer::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
  pointer-events: none;
}

:deep(.maritime-dialog .el-form-item__label) {
  color: #ffd65c;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.5px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.6);
  position: relative;
  padding-left: 12px;
  line-height: 32px;
}

:deep(.maritime-dialog .el-form-item__label::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: linear-gradient(180deg, #ffd65c, #ffb703);
  border-radius: 2px;
  box-shadow: 0 0 8px rgba(255, 214, 92, 0.6);
}

:deep(.maritime-dialog .el-form-item) {
  margin-bottom: 28px;
  position: relative;
}

:deep(.maritime-dialog .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  min-height: 36px;
}

:deep(.maritime-dialog .el-input__wrapper::before) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8));
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.6);
}

:deep(.maritime-dialog .el-input__wrapper:hover) {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.2);
  background: rgba(15, 43, 75, 0.7);
}

:deep(.maritime-dialog .el-input__wrapper.is-focus) {
  border-color: rgba(60, 235, 220, 0.7);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1);
  background: rgba(15, 43, 75, 0.8);
}

:deep(.maritime-dialog .el-input__wrapper.is-focus::before) {
  width: 100%;
}

:deep(.maritime-dialog .el-input__inner) {
  color: #e9f6ff;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.5;
}

:deep(.maritime-dialog .el-input__inner::placeholder) {
  color: rgba(233, 246, 255, 0.35);
  font-weight: 400;
}

:deep(.maritime-dialog .el-textarea__inner) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #e9f6ff;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
  border-radius: 10px;
}

:deep(.maritime-dialog .el-textarea__inner:hover) {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.2);
  background: rgba(15, 43, 75, 0.7);
}

:deep(.maritime-dialog .el-textarea__inner:focus) {
  border-color: rgba(60, 235, 220, 0.7);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1);
  background: rgba(15, 43, 75, 0.8);
}

:deep(.maritime-dialog .el-textarea__inner::placeholder) {
  color: rgba(233, 246, 255, 0.35);
}

:deep(.maritime-dialog .el-button) {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 12px 28px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.maritime-dialog .el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.maritime-dialog .el-button:hover::before) {
  width: 300px;
  height: 300px;
}

:deep(.maritime-dialog .el-button--default) {
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #4fa8ff;
}

:deep(.maritime-dialog .el-button--default:hover) {
  background: rgba(79, 168, 255, 0.2);
  border-color: rgba(79, 168, 255, 0.5);
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.3);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--danger) {
  background: linear-gradient(135deg, rgba(255, 90, 122, 0.9), rgba(255, 60, 100, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 90, 122, 0.3);
}

:deep(.maritime-dialog .el-button--danger:hover) {
  background: linear-gradient(135deg, rgba(255, 90, 122, 1), rgba(255, 60, 100, 1));
  box-shadow: 0 6px 25px rgba(255, 90, 122, 0.5);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--danger.is-loading) {
  background: linear-gradient(135deg, rgba(255, 90, 122, 0.7), rgba(255, 60, 100, 0.7));
}

@media (max-width: 768px) {
  .contract-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
