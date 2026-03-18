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
  background: var(--maritime-gradient-ocean);
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.back-button-wrapper {
  margin-bottom: 24px;
}

.back-button {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(30, 144, 255, 0.3);
  transition: all 0.3s;
}

.back-button:hover {
  background: white;
  border-color: var(--maritime-blue);
  transform: translateX(-5px);
}

.contract-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 800;
}

.contract-id {
  font-size: 14px;
  color: var(--maritime-gray);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.info-card {
  padding: 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--maritime-navy);
}

.card-title .el-icon {
  color: var(--maritime-blue);
}

.reject-title .el-icon {
  color: #f56c6c;
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
  background: var(--maritime-gray-light);
  border-radius: var(--maritime-radius-md);
}

.info-item .label {
  font-size: 14px;
  color: var(--maritime-gray);
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: var(--maritime-navy);
}

.info-item .value.price {
  color: var(--maritime-blue);
}

.detail-card {
  padding: 24px;
  margin-bottom: 24px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.detail-item {
  padding: 16px;
  background: rgba(30, 144, 255, 0.05);
  border-radius: var(--maritime-radius-md);
  border-left: 4px solid var(--maritime-blue);
}

.detail-label {
  font-size: 12px;
  color: var(--maritime-gray);
  margin-bottom: 8px;
}

.detail-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--maritime-navy);
}

.detail-value.price-large {
  font-size: 28px;
  color: var(--maritime-blue);
}

.purpose-card {
  padding: 24px;
  margin-bottom: 24px;
}

.purpose-text {
  margin: 0 0 24px 0;
  font-size: 16px;
  line-height: 1.8;
  color: var(--maritime-gray-dark);
  padding: 16px;
  background: var(--maritime-gray-light);
  border-radius: var(--maritime-radius-md);
}

.review-card {
  padding: 24px;
  margin-bottom: 24px;
  border: 2px solid #f56c6c;
}

.reject-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #f56c6c;
  padding: 16px;
  background: rgba(245, 108, 108, 0.1);
  border-radius: var(--maritime-radius-md);
}

.action-card {
  padding: 24px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 150px;
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
