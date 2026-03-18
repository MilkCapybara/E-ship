<template>
  <div class="ship-detail-page">
    <ParticleBackground />

    <div class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-button-wrapper">
        <el-button @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <!-- 船舶主要信息 -->
      <div class="ship-header maritime-card">
        <div class="ship-image-large">
          <img :src="ship.imageUrl || '/default-ship.jpg'" :alt="ship.shipName" />
          <div class="ship-status-badge" :class="`status-${ship.status?.toLowerCase()}`">
            {{ getStatusText(ship.status) }}
          </div>
        </div>

        <div class="ship-header-info">
          <h1 class="maritime-gradient-text">{{ ship.shipName }}</h1>

          <div class="ship-meta-row">
            <el-tag type="primary" size="large">{{ getShipTypeText(ship.shipType) }}</el-tag>
            <div class="rating-large">
              <el-icon color="#ffd700" :size="24"><Star /></el-icon>
              <span>{{ ship.rating || 5.0 }}</span>
            </div>
          </div>

          <div class="price-section">
            <span class="price-label">日租金</span>
            <span class="price-value">¥{{ ship.dailyRent?.toLocaleString() }}</span>
          </div>

          <div class="action-buttons">
            <el-button
              v-if="userStore.userInfo.role === 'RENTER'"
              type="primary"
              size="large"
              @click="handleRent"
              :disabled="ship.status !== 'AVAILABLE'"
            >
              <el-icon><Ship /></el-icon>
              立即租赁
            </el-button>
            <el-button
              v-if="userStore.userInfo.role === 'RENTER'"
              size="large"
              @click="toggleFavorite"
            >
              <el-icon :color="isFavorited ? '#ffd700' : ''">
                <component :is="isFavorited ? StarFilled : Star" />
              </el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="detail-grid">
        <!-- 基本信息 -->
        <div class="info-card maritime-card">
          <h3 class="card-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </h3>
          <div class="info-list">
            <div class="info-item">
              <span class="label">船舶类型</span>
              <span class="value">{{ getShipTypeText(ship.shipType) }}</span>
            </div>
            <div class="info-item">
              <span class="label">载重吨位</span>
              <span class="value">{{ ship.tonnage }} 吨</span>
            </div>
            <div class="info-item">
              <span class="label">建造年份</span>
              <span class="value">{{ ship.buildYear }}</span>
            </div>
            <div class="info-item">
              <span class="label">船级社</span>
              <span class="value">{{ ship.classificationSociety || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">船舶状态</span>
              <el-tag :type="getStatusTagType(ship.status)">
                {{ getStatusText(ship.status) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 船东信息 -->
        <div class="info-card maritime-card">
          <h3 class="card-title">
            <el-icon><User /></el-icon>
            船东信息
          </h3>
          <div class="info-list">
            <div class="info-item">
              <span class="label">船东</span>
              <span class="value">{{ ship.ownerName || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">公司名称</span>
              <span class="value">{{ ship.ownerCompany || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">信用评分</span>
              <el-progress
                :percentage="ship.ownerCreditScore || 100"
                :color="getCreditColor(ship.ownerCreditScore || 100)"
                :stroke-width="12"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 船舶描述 -->
      <div class="description-card maritime-card">
        <h3 class="card-title">
          <el-icon><Document /></el-icon>
          船舶描述
        </h3>
        <p class="description-text">
          {{ ship.description || '暂无描述' }}
        </p>
      </div>
    </div>

    <!-- 租赁对话框 -->
    <el-dialog
      v-model="showRentDialog"
      title="创建租赁合约"
      width="600px"
      class="maritime-dialog"
    >
      <el-form
        ref="rentFormRef"
        :model="rentForm"
        :rules="rentRules"
        label-width="100px"
      >
        <el-form-item label="船舶名称">
          <el-input :value="ship.shipName" disabled />
        </el-form-item>

        <el-form-item label="日租金">
          <el-input :value="`¥${ship.dailyRent?.toLocaleString()}`" disabled />
        </el-form-item>

        <el-form-item label="租赁日期" prop="dateRange">
          <MaritimeDatePicker
            v-model="rentForm.dateRange"
            type="daterange"
            placeholder="选择租赁日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="租赁用途" prop="purpose">
          <el-input
            v-model="rentForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请输入租赁用途"
          />
        </el-form-item>

        <el-form-item label="特殊要求" prop="specialRequirements">
          <el-input
            v-model="rentForm.specialRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求（可选）"
          />
        </el-form-item>

        <el-form-item label="预计总金额">
          <div class="total-amount">
            ¥{{ calculateTotalAmount().toLocaleString() }}
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRentDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRentSubmit" :loading="submitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, StarFilled, Ship, InfoFilled, User, Document } from '@element-plus/icons-vue'
import { getShipDetail } from '@/api/ship'
import { createContract } from '@/api/contract'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeDatePicker from '@/components/MaritimeDatePicker.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const ship = ref({})
const isFavorited = ref(false)
const showRentDialog = ref(false)
const submitting = ref(false)
const rentFormRef = ref()

const rentForm = reactive({
  dateRange: [],
  purpose: '',
  specialRequirements: ''
})

const rentRules = {
  dateRange: [{ required: true, message: '请选择租赁日期', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入租赁用途', trigger: 'blur' }]
}

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
    AVAILABLE: '可租',
    RENTED: '已租',
    MAINTENANCE: '维护中'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    AVAILABLE: 'success',
    RENTED: 'warning',
    MAINTENANCE: 'info'
  }
  return map[status] || 'info'
}

const getCreditColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const calculateTotalAmount = () => {
  if (!rentForm.dateRange || rentForm.dateRange.length !== 2 || !ship.value.dailyRent) {
    return 0
  }
  const start = new Date(rentForm.dateRange[0])
  const end = new Date(rentForm.dateRange[1])
  const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  return days * ship.value.dailyRent
}

const loadShipDetail = async () => {
  try {
    const res = await getShipDetail(route.params.id)
    ship.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载船舶详情失败')
  }
}

const checkFavoriteStatus = async () => {
  if (userStore.userInfo.role !== 'RENTER') return

  try {
    const res = await checkFavorite(route.params.id, userStore.userInfo.id)
    isFavorited.value = res.data
  } catch (error) {
    console.error('检查收藏状态失败', error)
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id, userStore.userInfo.id)
      ElMessage.success('已取消收藏')
      isFavorited.value = false
    } else {
      await addFavorite(userStore.userInfo.id, route.params.id)
      ElMessage.success('收藏成功')
      isFavorited.value = true
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleRent = () => {
  if (ship.value.status !== 'AVAILABLE') {
    ElMessage.warning('该船舶当前不可租赁')
    return
  }
  showRentDialog.value = true
}

const handleRentSubmit = async () => {
  await rentFormRef.value.validate()

  submitting.value = true
  try {
    await createContract(userStore.userInfo.id, {
      shipId: ship.value.id,
      startDate: rentForm.dateRange[0],
      endDate: rentForm.dateRange[1],
      dailyRent: ship.value.dailyRent,
      purpose: rentForm.purpose,
      specialRequirements: rentForm.specialRequirements
    })
    ElMessage.success('租赁申请已提交，等待船东审核')
    showRentDialog.value = false
    resetRentForm()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const resetRentForm = () => {
  Object.assign(rentForm, {
    dateRange: [],
    purpose: '',
    specialRequirements: ''
  })
  rentFormRef.value?.resetFields()
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadShipDetail()
  checkFavoriteStatus()
})
</script>

<style scoped>
.ship-detail-page {
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

.ship-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  padding: 32px;
  margin-bottom: 24px;
}

.ship-image-large {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: var(--maritime-radius-lg);
  overflow: hidden;
}

.ship-image-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ship-status-badge {
  position: absolute;
  top: 20px;
  left: 20px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.status-available {
  background: var(--maritime-gradient-primary);
}

.status-rented {
  background: var(--maritime-gradient-gold);
  color: var(--maritime-navy);
}

.status-maintenance {
  background: var(--maritime-gray);
}

.ship-header-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ship-header-info h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
}

.ship-meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rating-large {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--maritime-gold);
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  background: rgba(30, 144, 255, 0.1);
  border-radius: var(--maritime-radius-md);
  border-left: 4px solid var(--maritime-blue);
}

.price-label {
  font-size: 14px;
  color: var(--maritime-gray);
}

.price-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--maritime-blue);
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
}

.detail-grid {
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

.description-card {
  padding: 24px;
}

.description-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: var(--maritime-gray-dark);
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: var(--maritime-blue);
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
  .ship-header {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
