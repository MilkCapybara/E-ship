<template>
  <div class="search-page">
    <ParticleBackground />

    <!-- 搜索栏 -->
    <div class="search-header maritime-card">
      <h2 class="maritime-gradient-text">搜索船舶</h2>
      <p class="subtitle">找到最适合您的船舶</p>

      <div class="search-form">
        <div class="search-row">
          <MaritimeSelect
            v-model="searchForm.shipType"
            :options="shipTypeOptions"
            placeholder="船舶类型"
            style="width: 200px"
            @change="handleTypeChange"
          />

          <el-input
            v-model.number="searchForm.minRent"
            placeholder="最低租金"
            style="width: 150px"
          >
            <template #prepend>¥</template>
          </el-input>

          <el-input
            v-model.number="searchForm.maxRent"
            placeholder="最高租金"
            style="width: 150px"
          >
            <template #prepend>¥</template>
          </el-input>

          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索船舶名称"
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button type="primary" class="maritime-btn-primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <!-- 推荐船舶 -->
    <div class="recommended-section" v-if="recommendedShips.length > 0">
      <h3 class="section-title">
        <el-icon color="#ffd700"><Star /></el-icon>
        推荐船舶
      </h3>
      <div class="ships-carousel">
        <div
          v-for="ship in recommendedShips"
          :key="ship.id"
          class="ship-card-small maritime-card maritime-shimmer"
          @click="viewShipDetail(ship)"
        >
          <img :src="ship.imageUrl || '/default-ship.jpg'" :alt="ship.shipName" />
          <div class="ship-info-small">
            <h4>{{ ship.shipName }}</h4>
            <div class="rating">
              <el-icon color="#ffd700"><Star /></el-icon>
              <span>{{ ship.rating }}</span>
            </div>
            <div class="price">¥{{ ship.dailyRent.toLocaleString() }}/天</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div class="results-header">
        <h3>搜索结果 ({{ total }})</h3>
        <MaritimeSelect
          v-model="sortBy"
          :options="sortOptions"
          placeholder="排序方式"
          style="width: 150px"
          @change="handleSortChange"
        />
      </div>

      <div class="ships-grid">
        <div
          v-for="ship in ships"
          :key="ship.id"
          class="ship-card maritime-card maritime-float"
          @click="viewShipDetail(ship)"
        >
          <div class="ship-image">
            <img :src="ship.imageUrl || '/default-ship.jpg'" :alt="ship.shipName" />
            <div class="ship-status status-available">
              {{ getStatusText(ship.status) }}
            </div>
            <div class="favorite-btn" @click.stop="toggleFavorite(ship)">
              <el-icon :color="ship.isFavorite ? '#ffd700' : '#fff'" :size="24">
                <component :is="ship.isFavorite ? 'StarFilled' : 'Star'" />
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
            <el-button type="primary" class="rent-btn" @click.stop="rentShip(ship)">
              立即租赁
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="ships.length === 0 && !loading" description="暂无符合条件的船舶" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleSearch"
          @size-change="handleSearch"
        />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Star, StarFilled } from '@element-plus/icons-vue'
import { searchShips, getRecommendedShips } from '@/api/ship'
import { createContract } from '@/api/contract'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeSelect from '@/components/MaritimeSelect.vue'
import MaritimeDatePicker from '@/components/MaritimeDatePicker.vue'
import wsService from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const ships = ref([])
const recommendedShips = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortBy = ref('')
const showRentDialog = ref(false)
const selectedShip = ref(null)
const submitting = ref(false)
const rentFormRef = ref()

const searchForm = reactive({
  shipType: '',
  minRent: null,
  maxRent: null,
  minTonnage: null,
  maxTonnage: null,
  keyword: ''
})

const rentForm = reactive({
  dateRange: [],
  purpose: '',
  specialRequirements: ''
})

const shipTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '集装箱船', value: 'CONTAINER' },
  { label: '散货船', value: 'BULK' },
  { label: '油船', value: 'TANKER' },
  { label: '客船', value: 'PASSENGER' }
]

const sortOptions = [
  { label: '全部船舶', value: '' },
  { label: '评分最高', value: 'rating' },
  { label: '价格最低', value: 'dailyRent' },
  { label: '最新发布', value: 'createdAt' }
]

const rentRules = {
  dateRange: [{ required: true, message: '请选择租赁日期', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入租赁用途', trigger: 'blur' }]
}

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
  // 禁用今天及之前的日期，只允许选择未来日期
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

const loadRecommended = async () => {
  try {
    const res = await getRecommendedShips(6)
    recommendedShips.value = res.data
  } catch (error) {
    console.error('加载推荐船舶失败:', error)
  }
}

const handleSearch = async () => {
  loading.value = true
  console.log('开始搜索，参数:', {
    ...searchForm,
    page: currentPage.value,
    size: pageSize.value,
    sortBy: sortBy.value
  })
  try {
    const res = await searchShips({
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value
    })
    console.log('搜索结果:', res)
    ships.value = res.data.records
    total.value = res.data.total

    // 检查收藏状态
    for (const ship of ships.value) {
      try {
        const favRes = await checkFavorite(ship.id, userStore.userInfo.id)
        ship.isFavorite = favRes.data
      } catch (error) {
        ship.isFavorite = false
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = () => {
  currentPage.value = 1
  handleSearch()
}

const handleSortChange = () => {
  currentPage.value = 1
  handleSearch()
}

const toggleFavorite = async (ship) => {
  try {
    if (ship.isFavorite) {
      await removeFavorite(ship.id, userStore.userInfo.id)
      ship.isFavorite = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(userStore.userInfo.id, ship.id)
      ship.isFavorite = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const rentShip = (ship) => {
  console.log('点击租赁按钮，船舶信息:', ship)

  if (ship.status !== 'AVAILABLE') {
    ElMessage.warning('该船舶当前不可租赁')
    return
  }

  selectedShip.value = ship
  showRentDialog.value = true
  console.log('租赁对话框已打开')
}

const handleRent = async () => {
  console.log('开始提交租赁申请')

  // 表单验证
  try {
    await rentFormRef.value.validate()
    console.log('表单验证通过')
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.warning('请填写完整的租赁信息')
    return
  }

  submitting.value = true
  console.log('准备提交合约数据:', {
    renterId: userStore.userInfo.id,
    shipId: selectedShip.value.id,
    startDate: rentForm.dateRange[0],
    endDate: rentForm.dateRange[1],
    dailyRent: selectedShip.value.dailyRent,
    purpose: rentForm.purpose,
    specialRequirements: rentForm.specialRequirements
  })

  try {
    const res = await createContract(userStore.userInfo.id, {
      shipId: selectedShip.value.id,
      startDate: rentForm.dateRange[0],
      endDate: rentForm.dateRange[1],
      dailyRent: selectedShip.value.dailyRent,
      purpose: rentForm.purpose,
      specialRequirements: rentForm.specialRequirements
    })
    console.log('合约创建成功:', res)
    ElMessage.success('租赁申请已提交，等待船东审核')
    showRentDialog.value = false
    resetRentForm()

    // 刷新搜索结果
    await handleSearch()
  } catch (error) {
    console.error('提交失败，完整错误:', error)
    console.error('错误响应:', error.response)
    const errorMsg = error.response?.data?.message || error.message || '提交失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

const viewShipDetail = (ship) => {
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
  loadRecommended()
  handleSearch()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('renter-search', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('renter-search')
})
</script>

<style scoped>
/* 页面容器 */
.search-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
  overflow-y: auto;
  max-height: calc(100vh - 60px);
}

/* 搜索头部 */
.search-header {
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
  z-index: 10;
  overflow: visible;
}

.search-header::before {
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

.search-header h2 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffd65c 0%, #ffb703 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(255, 214, 92, 0.3);
  letter-spacing: 1px;
  position: relative;
  z-index: 1;
}

.subtitle {
  color: rgba(233, 246, 255, 0.7);
  margin: 0 0 24px 0;
  font-size: 15px;
  position: relative;
  z-index: 1;
}

.search-form {
  margin-top: 24px;
  position: relative;
  z-index: 10;
}

.search-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  position: relative;
  z-index: 10;
}

.search-row :deep(.el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  transition: all 0.3s ease;
}

.search-row :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.2);
}

.search-row :deep(.el-input__inner) {
  color: #e9f6ff;
}

.maritime-btn-primary {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.9),
    rgba(255, 183, 3, 0.9)) !important;
  border: none !important;
  color: #051423 !important;
  font-weight: 700;
  transition: all 0.3s ease;
}

.maritime-btn-primary:hover {
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
  transform: translateY(-2px);
}

/* 推荐区域 */
.recommended-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
}

.ships-carousel {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.ship-card-small {
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.ship-card-small:hover {
  transform: translateY(-4px);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4), 0 0 20px rgba(255, 214, 92, 0.3);
}

.ship-card-small img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.ship-info-small {
  padding: 12px;
}

.ship-info-small h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #ffd65c;
}

.ship-info-small .rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #ffd65c;
}

.ship-info-small .price {
  font-size: 18px;
  font-weight: 700;
  color: #4fa8ff;
}

/* 搜索结果 */
.search-results {
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.85) 0%,
    rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(79, 168, 255, 0.2);
}

.results-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.ships-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.ship-card {
  cursor: pointer;
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
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4), 0 0 30px rgba(255, 214, 92, 0.3);
}

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
}

.status-available {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9));
  color: #051423;
  border: 1px solid rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
}

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
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  z-index: 2;
}

.favorite-btn:hover {
  background: rgba(6, 22, 39, 0.95);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.3);
  transform: scale(1.1);
}

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

.rent-btn {
  width: 100%;
  background: linear-gradient(135deg,
    rgba(79, 168, 255, 0.9),
    rgba(79, 168, 255, 0.95)) !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
  transition: all 0.3s ease;
}

.rent-btn:hover {
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.5);
  transform: translateY(-2px);
}

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
  margin-top: 24px;
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

/* 对话框整体样式优化 */
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
  color: var(--yellow-400);
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

/* 滚动条样式 */
.search-page::-webkit-scrollbar {
  width: 8px;
}

.search-page::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.search-page::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.6),
    rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

.search-page::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.8),
    rgba(79, 168, 255, 0.8));
}
</style>
