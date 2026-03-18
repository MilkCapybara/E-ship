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
}

/* 对话框样式 */
:deep(.maritime-dialog .el-dialog) {
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.95) 0%,
    rgba(11, 31, 54, 0.95) 100%);
  border: 2px solid rgba(79, 168, 255, 0.4);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.8), 0 0 80px rgba(79, 168, 255, 0.3);
}

:deep(.maritime-dialog .el-dialog__header) {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.95),
    rgba(255, 183, 3, 0.95));
  color: #051423;
  padding: 24px;
  border-bottom: 2px solid rgba(255, 214, 92, 0.5);
}

:deep(.maritime-dialog .el-dialog__title) {
  color: #051423;
  font-weight: 800;
  font-size: 20px;
}

:deep(.maritime-dialog .el-dialog__close) {
  color: #051423;
  font-size: 20px;
}

:deep(.maritime-dialog .el-dialog__body) {
  color: #e9f6ff;
}

:deep(.maritime-dialog .el-form-item__label) {
  color: #ffd65c;
  font-weight: 600;
}

:deep(.maritime-dialog .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
}

:deep(.maritime-dialog .el-input__inner) {
  color: #e9f6ff;
}

:deep(.maritime-dialog .el-textarea__inner) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #e9f6ff;
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
