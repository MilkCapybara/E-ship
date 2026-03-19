<template>
  <div class="favorites-page">
    <ParticleBackground />

    <div class="page-header maritime-card">
      <div class="header-content">
        <div>
          <h2 class="maritime-gradient-text">我的收藏</h2>
          <p class="subtitle">您收藏的船舶</p>
        </div>
        <el-badge :value="total" :max="99" class="badge-item">
          <el-icon :size="32" color="#ffd700"><Star /></el-icon>
        </el-badge>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="ships-grid">
      <div
        v-for="ship in ships"
        :key="ship.id"
        class="ship-card maritime-card maritime-float"
      >
        <div class="ship-image">
          <img :src="ship.imageUrl || '/default-ship.jpg'" :alt="ship.shipName" />
          <div class="ship-status" :class="`status-${ship.status.toLowerCase()}`">
            {{ getStatusText(ship.status) }}
          </div>
          <div class="favorite-btn" @click.stop="handleRemove(ship)">
            <el-icon color="#ffd700" :size="24">
              <StarFilled />
            </el-icon>
          </div>
        </div>
        <div class="ship-info">
          <h3>{{ ship.shipName }}</h3>
          <div class="ship-meta">
            <el-tag type="info">{{ getShipTypeText(ship.shipType) }}</el-tag>
            <div class="rating">
              <el-icon color="#ffd700"><Star /></el-icon>
              <span>{{ ship.rating }}</span>
            </div>
          </div>
          <div class="ship-details">
            <div class="detail-item">
              <span class="label">载重吨位</span>
              <span class="value">{{ ship.tonnage }} 吨</span>
            </div>
            <div class="detail-item">
              <span class="label">建造年份</span>
              <span class="value">{{ ship.buildYear }}</span>
            </div>
          </div>
          <div class="ship-price">
            <span class="price-label">日租金</span>
            <span class="price-value">¥{{ ship.dailyRent.toLocaleString() }}</span>
          </div>
          <div class="ship-actions">
            <el-button type="primary" @click="rentShip(ship)">
              立即租赁
            </el-button>
            <el-button @click="viewDetail(ship)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="ships.length === 0 && !loading" description="暂无收藏的船舶">
      <el-button type="primary" @click="$router.push('/renter/search')">
        去搜索船舶
      </el-button>
    </el-empty>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadFavorites"
        @size-change="loadFavorites"
      />
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
          <el-input :value="selectedShip?.shipName" disabled />
        </el-form-item>

        <el-form-item label="日租金">
          <el-input :value="`¥${selectedShip?.dailyRent.toLocaleString()}`" disabled />
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
        <el-button type="primary" @click="handleRent" :loading="submitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getMyFavorites, removeFavorite } from '@/api/favorite'
import { createContract } from '@/api/contract'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeDatePicker from '@/components/MaritimeDatePicker.vue'
import wsService from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const ships = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const showRentDialog = ref(false)
const selectedShip = ref(null)
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

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const calculateTotalAmount = () => {
  if (!rentForm.dateRange || rentForm.dateRange.length !== 2 || !selectedShip.value) {
    return 0
  }
  const start = new Date(rentForm.dateRange[0])
  const end = new Date(rentForm.dateRange[1])
  const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  return days * selectedShip.value.dailyRent
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites({
      userId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value
    })
    ships.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (ship) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这艘船舶吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await removeFavorite(ship.id, userStore.userInfo.id)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const rentShip = (ship) => {
  if (ship.status !== 'AVAILABLE') {
    ElMessage.warning('该船舶当前不可租赁')
    return
  }
  selectedShip.value = ship
  showRentDialog.value = true
}

const handleRent = async () => {
  await rentFormRef.value.validate()

  submitting.value = true
  try {
    await createContract(userStore.userInfo.id, {
      shipId: selectedShip.value.id,
      startDate: rentForm.dateRange[0],
      endDate: rentForm.dateRange[1],
      dailyRent: selectedShip.value.dailyRent,
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

const viewDetail = (ship) => {
  router.push(`/ship/${ship.id}`)
}

const resetRentForm = () => {
  Object.assign(rentForm, {
    dateRange: [],
    purpose: '',
    specialRequirements: ''
  })
  rentFormRef.value?.resetFields()
}

// WebSocket消息处理
const handleWebSocketMessage = (notification) => {
  if (notification.type === 'CONTRACT_UPDATE') {
    const { action } = notification.data

    if (action === 'APPROVED') {
      ElMessage.success('您的租赁申请已被同意！')
    } else if (action === 'REJECTED') {
      ElMessage.warning('您的租赁申请被拒绝了')
    }
  }
}

onMounted(() => {
  loadFavorites()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('renter-favorites', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('renter-favorites')
})
</script>

<style scoped>
/* 页面容器 */
.favorites-page {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.badge-item {
  cursor: pointer;
  position: relative;
}

.badge-item :deep(.el-badge__content) {
  background: linear-gradient(135deg, #ffd65c, #ffb703);
  border: 2px solid rgba(255, 214, 92, 0.5);
  color: #051423;
  font-weight: 700;
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
}

/* 船舶网格 */
.ships-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* 船舶卡片 */
.ship-card {
  overflow: hidden;
  transition: all 0.3s ease;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  position: relative;
}

.ship-card::before {
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

.ship-card:hover::before {
  left: 100%;
}

.ship-card:hover {
  transform: translateY(-8px);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(255, 214, 92, 0.3);
}

/* 船舶图片 */
.ship-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(15, 43, 75, 0.8), rgba(6, 22, 39, 0.9));
}

.ship-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.ship-card:hover .ship-image img {
  transform: scale(1.1);
}

/* 船舶状态 */
.ship-status {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(5px);
  z-index: 2;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.status-available {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9));
  color: #051423;
  border: 1px solid rgba(255, 214, 92, 0.5);
}

.status-rented {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.9), rgba(230, 162, 60, 0.7));
  border: 1px solid rgba(230, 162, 60, 0.5);
}

.status-maintenance {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.9), rgba(144, 147, 153, 0.7));
  border: 1px solid rgba(144, 147, 153, 0.5);
}

/* 收藏按钮 */
.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(6, 22, 39, 0.8);
  border: 1px solid rgba(255, 214, 92, 0.5);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  z-index: 2;
}

.favorite-btn:hover {
  background: rgba(6, 22, 39, 0.95);
  border-color: rgba(255, 214, 92, 0.8);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
  transform: scale(1.1);
}

/* 船舶信息 */
.ship-info {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.ship-info h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
}

.ship-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  color: #ffd65c;
}

.ship-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(79, 168, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(79, 168, 255, 0.1);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 12px;
  color: rgba(233, 246, 255, 0.7);
}

.detail-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #e9f6ff;
}

.ship-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
  border-bottom: 1px solid rgba(79, 168, 255, 0.2);
  margin-bottom: 16px;
}

.price-label {
  color: rgba(233, 246, 255, 0.7);
  font-size: 14px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
}

.ship-actions {
  display: flex;
  gap: 8px;
}

.ship-actions .el-button {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #e9f6ff;
  transition: all 0.3s ease;
}

.ship-actions .el-button:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(255, 183, 3, 0.2));
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.3);
  transform: translateY(-2px);
}

.ship-actions .el-button--primary {
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.9),
    rgba(79, 168, 255, 0.95));
  border: none;
  color: white;
}

.ship-actions .el-button--primary:hover {
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.5);
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

.total-amount {
  font-size: 28px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
  letter-spacing: 1px;
}

/* 对话框整体样式优化 - 毛玻璃航运金融科技风 */
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

/* 对话框外发光效果 */
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

/* 标题区域装饰图案 */
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

/* 对话框标题装饰 */
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

/* 对话框背景装饰 */
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

/* 对话框底部装饰 */
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

/* 表单标签装饰 */
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

/* 输入框装饰线 */
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

/* 按钮样式 */
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

/* 空状态 */
:deep(.el-empty) {
  padding: 60px;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

:deep(.el-empty__description) {
  color: rgba(233, 246, 255, 0.7);
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
.favorites-page::-webkit-scrollbar {
  width: 8px;
}

.favorites-page::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.favorites-page::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.6),
    rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

.favorites-page::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.8),
    rgba(79, 168, 255, 0.8));
}
</style>
