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
      append-to-body
      :close-on-click-modal="false"
      :z-index="3000"
    >
      <el-form
        ref="rentFormRef"
        :model="rentForm"
        :rules="rentRules"
        label-width="100px"
      >
        <el-form-item label="船舶名称">
          <el-input :value="ship?.shipName" disabled />
        </el-form-item>

        <el-form-item label="日租金">
          <el-input :value="`¥${ship?.dailyRent?.toLocaleString()}`" disabled />
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
  background: linear-gradient(135deg, #0a1929 0%, #1a2332 50%, #0d1b2a 100%);
  overflow: hidden;
}

.ship-detail-page::before {
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

.ship-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
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

.ship-header::before {
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

.ship-image-large {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 40px rgba(60, 235, 220, 0.2);
}

.ship-image-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.ship-image-large:hover img {
  transform: scale(1.05);
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
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.status-available {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.9), rgba(79, 168, 255, 0.9));
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.5);
}

.status-rented {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.9), rgba(255, 180, 50, 0.9));
  color: #1a1a1a;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
}

.status-maintenance {
  background: rgba(79, 168, 255, 0.5);
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.3);
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
  background: linear-gradient(135deg, #3cebdc 0%, #4fa8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(60, 235, 220, 0.3);
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
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  background: rgba(10, 14, 39, 0.5);
  border-radius: 12px;
  border-left: 4px solid #3cebdc;
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.1);
}

.price-label {
  font-size: 14px;
  color: rgba(233, 246, 255, 0.6);
  letter-spacing: 0.5px;
}

.price-value {
  font-size: 32px;
  font-weight: 700;
  color: #3cebdc;
  text-shadow: 0 0 20px rgba(60, 235, 220, 0.5);
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
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

.action-buttons .el-button--primary {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.9), rgba(79, 168, 255, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(60, 235, 220, 0.3);
}

.action-buttons .el-button--primary:hover {
  background: linear-gradient(135deg, rgba(60, 235, 220, 1), rgba(79, 168, 255, 1));
  box-shadow: 0 6px 25px rgba(60, 235, 220, 0.5);
  transform: translateY(-2px);
}

.action-buttons .el-button--primary:disabled {
  background: rgba(79, 168, 255, 0.3);
  color: rgba(255, 255, 255, 0.5);
}

.action-buttons .el-button--default {
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #4fa8ff;
}

.action-buttons .el-button--default:hover {
  background: rgba(79, 168, 255, 0.2);
  border-color: rgba(79, 168, 255, 0.5);
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.3);
  transform: translateY(-2px);
}

.detail-grid {
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

.description-card {
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

.description-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.description-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(233, 246, 255, 0.8);
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
  letter-spacing: 1px;
}

/* 租赁对话框 - 毛玻璃航运金融科技风 */
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

:deep(.maritime-dialog .el-form-item__content) {
  position: relative;
  line-height: normal;
  display: block;
}

/* 确保错误提示不会与输入框重叠 */
:deep(.maritime-dialog .el-form-item.is-error .el-input__wrapper) {
  border-color: rgba(255, 90, 122, 0.6);
  box-shadow: 0 0 15px rgba(255, 90, 122, 0.3);
}

:deep(.maritime-dialog .el-form-item.is-error .el-textarea__inner) {
  border-color: rgba(255, 90, 122, 0.6);
  box-shadow: 0 0 15px rgba(255, 90, 122, 0.3);
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
  font-weight: 500;
  line-height: 1.6;
  border-radius: 10px;
  font-size: 14px;
  position: relative;
  min-height: 100px;
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
  font-weight: 400;
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

:deep(.maritime-dialog .el-button--primary) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.9), rgba(79, 168, 255, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(60, 235, 220, 0.3);
}

:deep(.maritime-dialog .el-button--primary:hover) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 1), rgba(79, 168, 255, 1));
  box-shadow: 0 6px 25px rgba(60, 235, 220, 0.5);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--primary.is-loading) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.7), rgba(79, 168, 255, 0.7));
}

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
  letter-spacing: 1px;
}

/* 下拉菜单样式 - 确保不被遮挡 */
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

@media (max-width: 768px) {
  .ship-header {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
