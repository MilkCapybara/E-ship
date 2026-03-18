<template>
  <div class="ships-page">
    <ParticleBackground />

    <!-- 流光线条效果 -->
    <div class="stream-lines">
      <div class="stream-line" v-for="i in 5" :key="i" :style="{ left: `${i * 20}%`, animationDelay: `${i * 0.5}s` }"></div>
    </div>

    <!-- 顶部装饰光带 -->
    <div class="top-decoration">
      <div class="decoration-beam"></div>
      <div class="decoration-dots">
        <span v-for="i in 20" :key="i" class="dot" :style="{ animationDelay: `${i * 0.1}s` }"></span>
      </div>
    </div>

    <div class="page-header maritime-card">
      <div class="header-content">
        <div>
          <h2 class="maritime-gradient-text">我的船舶</h2>
          <p class="subtitle">管理您的船舶资产</p>
        </div>
        <el-button type="primary" class="tech-btn-primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加船舶
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar maritime-card">
      <MaritimeSelect
        v-model="filterType"
        :options="shipTypeOptions"
        placeholder="船舶类型"
        style="width: 200px"
      />
      <el-button class="tech-btn-search" @click="loadShips">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 船舶列表 -->
    <div class="ships-grid">
      <div
        v-for="ship in ships"
        :key="ship.id"
        class="ship-card maritime-card maritime-float"
        @click="viewShipDetail(ship)"
      >
        <div class="ship-image">
          <img :src="ship.imageUrl || '/default-ship.jpg'" :alt="ship.shipName" />
          <div class="ship-status" :class="`status-${ship.status.toLowerCase()}`">
            {{ getStatusText(ship.status) }}
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
          <div class="ship-price">
            <span class="price-label">日租金</span>
            <span class="price-value">¥{{ ship.dailyRent.toLocaleString() }}</span>
          </div>
          <div class="ship-actions">
            <el-button size="small" @click.stop="editShip(ship)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click.stop="handleDelete(ship)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="ships.length === 0 && !loading" description="暂无船舶数据，请先添加船舶" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadShips"
        @size-change="loadShips"
      />
    </div>

    <!-- 页面底部装饰 -->
    <div class="page-footer">
      <div class="footer-decoration">
        <div class="decoration-line"></div>
        <div class="decoration-text">航运科技 · 智慧管理</div>
        <div class="decoration-line"></div>
      </div>
    </div>

    <!-- 添加/编辑船舶对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingShip ? '编辑船舶' : '添加船舶'"
      width="600px"
      class="maritime-dialog"
    >
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="100px"
      >
        <el-form-item label="船舶名称" prop="shipName">
          <el-input v-model="shipForm.shipName" placeholder="请输入船舶名称" />
        </el-form-item>

        <el-form-item label="船舶类型" prop="shipType">
          <el-select
            v-model="shipForm.shipType"
            placeholder="请选择船舶类型"
            style="width: 100%"
          >
            <el-option
              v-for="option in shipTypeOptions.filter(opt => opt.value !== '')"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="载重吨位" prop="tonnage">
          <el-input v-model.number="shipForm.tonnage" placeholder="请输入载重吨位">
            <template #append>吨</template>
          </el-input>
        </el-form-item>

        <el-form-item label="建造年份" prop="buildYear">
          <el-input v-model.number="shipForm.buildYear" placeholder="请输入建造年份" />
        </el-form-item>

        <el-form-item label="船级社认证" prop="classificationSociety">
          <el-input v-model="shipForm.classificationSociety" placeholder="请输入船级社认证" />
        </el-form-item>

        <el-form-item label="日租金" prop="dailyRent">
          <el-input v-model.number="shipForm.dailyRent" placeholder="请输入日租金">
            <template #append>元/天</template>
          </el-input>
        </el-form-item>

        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="shipForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>

        <el-form-item label="船舶描述" prop="description">
          <el-input
            v-model="shipForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入船舶描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ editingShip ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Star, Edit, Delete } from '@element-plus/icons-vue'
import { getMyShips, addShip, updateShip, deleteShip } from '@/api/ship'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import ParticleBackground from '@/components/ParticleBackground.vue'
import MaritimeSelect from '@/components/MaritimeSelect.vue'
import wsService from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const ships = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('')
const showAddDialog = ref(false)
const editingShip = ref(null)
const submitting = ref(false)
const shipFormRef = ref()

const shipTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '集装箱船', value: 'CONTAINER' },
  { label: '散货船', value: 'BULK' },
  { label: '油船', value: 'TANKER' },
  { label: '客船', value: 'PASSENGER' }
]

const shipForm = reactive({
  shipName: '',
  shipType: '',
  tonnage: null,
  buildYear: null,
  classificationSociety: '',
  dailyRent: null,
  imageUrl: '',
  description: ''
})

const shipRules = {
  shipName: [{ required: true, message: '请输入船舶名称', trigger: 'blur' }],
  shipType: [{ required: true, message: '请选择船舶类型', trigger: 'change' }],
  tonnage: [{ required: true, message: '请输入载重吨位', trigger: 'blur' }],
  buildYear: [{ required: true, message: '请输入建造年份', trigger: 'blur' }],
  dailyRent: [{ required: true, message: '请输入日租金', trigger: 'blur' }]
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

const loadShips = async () => {
  loading.value = true
  try {
    const res = await getMyShips({
      ownerId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      shipType: filterType.value || undefined
    })
    ships.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载船舶列表失败')
  } finally {
    loading.value = false
  }
}

const editShip = (ship) => {
  editingShip.value = ship
  Object.assign(shipForm, {
    shipName: ship.shipName,
    shipType: ship.shipType,
    tonnage: ship.tonnage,
    buildYear: ship.buildYear,
    classificationSociety: ship.classificationSociety,
    dailyRent: ship.dailyRent,
    imageUrl: ship.imageUrl,
    description: ship.description
  })
  showAddDialog.value = true
}

const handleSubmit = async () => {
  await shipFormRef.value.validate()

  submitting.value = true
  try {
    if (editingShip.value) {
      await updateShip(editingShip.value.id, userStore.userInfo.id, shipForm)
      ElMessage.success('船舶更新成功')
    } else {
      await addShip(userStore.userInfo.id, shipForm)
      ElMessage.success('船舶添加成功')
    }
    showAddDialog.value = false
    resetForm()
    loadShips()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (ship) => {
  try {
    await ElMessageBox.confirm('确定要删除这艘船舶吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteShip(ship.id, userStore.userInfo.id)
    ElMessage.success('删除成功')
    loadShips()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const viewShipDetail = (ship) => {
  router.push(`/ship/${ship.id}`)
}

const resetForm = () => {
  editingShip.value = null
  Object.assign(shipForm, {
    shipName: '',
    shipType: '',
    tonnage: null,
    buildYear: null,
    classificationSociety: '',
    dailyRent: null,
    imageUrl: '',
    description: ''
  })
  shipFormRef.value?.resetFields()
}

// WebSocket消息处理
const handleWebSocketMessage = (notification) => {
  if (notification.type === 'CONTRACT_UPDATE') {
    const { action } = notification.data

    if (action === 'NEW_CONTRACT') {
      ElMessage.success('收到新的租赁申请！')
    } else if (action === 'CANCELLED') {
      ElMessage.info('租家取消了一个合约申请')
    }
  }
}

onMounted(() => {
  loadShips()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('owner-ships', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('owner-ships')
})
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Rajdhani:wght@400;600;700;800&family=Saira+Condensed:wght@400;600;700&display=swap");

/* 科技风配色变量 */
:root {
  --navy-950: #061627;
  --navy-900: #0b1f36;
  --navy-800: #0f2b4b;
  --cyan-400: #3cebdc;
  --cyan-300: #7ef7f0;
  --blue-400: #4fa8ff;
  --yellow-400: #ffd65c;
  --yellow-500: #ffb703;
  --glass: rgba(9, 28, 52, 0.72);
  --glass-border: rgba(77, 186, 255, 0.28);
  --text-main: #e9f6ff;
  --text-muted: rgba(233, 246, 255, 0.68);
}

.ships-page {
  position: relative;
  min-height: 100vh;
  max-height: calc(100vh - 60px);
  padding: 32px;
  font-family: "Saira Condensed", "Rajdhani", "Segoe UI", sans-serif;
  background: radial-gradient(circle at 20% 20%, rgba(60, 235, 220, 0.08), transparent 40%),
    radial-gradient(circle at 80% 30%, rgba(79, 168, 255, 0.12), transparent 45%),
    linear-gradient(130deg, #061624 0%, #0b223d 45%, #0f2c50 100%);
  color: var(--text-main);
  overflow-x: hidden;
  overflow-y: auto;
}

/* 背景网格效果 */
.ships-page::before {
  content: '';
  position: fixed;
  inset: 0;
  background: linear-gradient(180deg, rgba(6, 22, 39, 0.8) 0%, rgba(6, 22, 39, 0.1) 40%, rgba(0, 0, 0, 0) 100%),
    repeating-linear-gradient(90deg, rgba(79, 168, 255, 0.05) 0, rgba(79, 168, 255, 0.05) 1px, transparent 1px, transparent 120px);
  pointer-events: none;
  z-index: 0;
}

/* 背景光晕 */
.ships-page::after {
  content: '';
  position: fixed;
  inset: 0;
  background-image: radial-gradient(circle at center, rgba(60, 235, 220, 0.1) 0, transparent 55%);
  pointer-events: none;
  z-index: 0;
  animation: grid-pulse 4s ease-in-out infinite;
}

@keyframes grid-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* 流光线条效果 */
.stream-lines {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

.stream-line {
  position: absolute;
  top: -100%;
  width: 2px;
  height: 100px;
  background: linear-gradient(180deg, transparent, rgba(60, 235, 220, 0.8), rgba(79, 168, 255, 0.8), transparent);
  animation: stream-fall 3s linear infinite;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.8);
}

@keyframes stream-fall {
  0% {
    top: -100%;
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}

.stream-line:nth-child(1) { left: 10%; animation-duration: 2.5s; }
.stream-line:nth-child(2) { left: 30%; animation-duration: 3s; animation-delay: 0.5s; }
.stream-line:nth-child(3) { left: 50%; animation-duration: 2.8s; animation-delay: 1s; }
.stream-line:nth-child(4) { left: 70%; animation-duration: 3.2s; animation-delay: 1.5s; }
.stream-line:nth-child(5) { left: 90%; animation-duration: 2.7s; animation-delay: 2s; }

/* 顶部装饰光带 */
.top-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  z-index: 100;
  pointer-events: none;
  overflow: hidden;
}

.decoration-beam {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    var(--cyan-400) 25%,
    var(--yellow-400) 50%,
    var(--cyan-400) 75%,
    transparent 100%);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.8), 0 0 40px rgba(60, 235, 220, 0.4);
  animation: beam-flow 3s linear infinite;
}

@keyframes beam-flow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.decoration-dots {
  position: absolute;
  top: 8px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 0 5%;
}

.decoration-dots .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--cyan-400);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.8);
  animation: dot-pulse 2s ease-in-out infinite;
}

@keyframes dot-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.5);
  }
}

.page-header {
  position: relative;
  z-index: 2;
  margin-bottom: 28px;
  padding: 28px 32px;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: 0 0 30px rgba(9, 35, 62, 0.6), 0 0 60px rgba(60, 235, 220, 0.15);
  backdrop-filter: blur(12px);
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(60, 235, 220, 0.15), transparent);
  animation: card-shine 6s infinite;
  pointer-events: none;
}

/* 页面头部装饰 */
.page-header::after {
  content: '';
  position: absolute;
  top: 50%;
  right: -100px;
  transform: translateY(-50%);
  width: 200px;
  height: 200px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><circle cx="100" cy="100" r="80" fill="none" stroke="rgba(60,235,220,0.2)" stroke-width="2" stroke-dasharray="8,6"/><circle cx="100" cy="100" r="60" fill="none" stroke="rgba(79,168,255,0.25)" stroke-width="2"/><circle cx="100" cy="100" r="40" fill="none" stroke="rgba(255,214,92,0.2)" stroke-width="1.5" stroke-dasharray="4,4"/><path d="M70,100 L80,95 L90,98 L100,92 L110,95 L120,90 L130,93" fill="none" stroke="rgba(60,235,220,0.3)" stroke-width="2"/></svg>') no-repeat center;
  background-size: contain;
  pointer-events: none;
  opacity: 0.4;
  animation: rotate-decoration 20s linear infinite;
}

@keyframes rotate-decoration {
  from { transform: translateY(-50%) rotate(0deg); }
  to { transform: translateY(-50%) rotate(360deg); }
}

@keyframes card-shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #ffd65c;
  text-shadow: 0 0 30px rgba(255, 214, 92, 0.8), 0 0 60px rgba(255, 214, 92, 0.4);
  animation: value-glow 2s ease-in-out infinite;
}

@keyframes value-glow {
  0%, 100% { filter: drop-shadow(0 0 15px rgba(60, 235, 220, 0.5)); }
  50% { filter: drop-shadow(0 0 25px rgba(60, 235, 220, 0.8)) drop-shadow(0 0 40px rgba(60, 235, 220, 0.4)); }
}

.subtitle {
  color: #ffd65c;
  margin: 0;
  font-size: 14px;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.filter-bar {
  position: relative;
  z-index: 10;
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 28px;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 14px;
  box-shadow: 0 0 22px rgba(79, 168, 255, 0.15);
  backdrop-filter: blur(12px);
  overflow: visible;
}

/* 筛选栏装饰效果 */
.filter-bar::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.2), transparent);
  animation: filter-shine 3s ease-in-out infinite;
}

@keyframes filter-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

.filter-bar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyan-400), var(--yellow-400), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

.ships-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 28px;
  margin-bottom: 32px;
}

.ship-card {
  position: relative;
  cursor: pointer;
  overflow: visible;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: 0 0 25px rgba(9, 35, 62, 0.6);
  backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 卡片边框流光效果 */
.ship-card::before {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(45deg,
    transparent 0%,
    rgba(60, 235, 220, 0.5) 25%,
    rgba(79, 168, 255, 0.5) 50%,
    rgba(255, 214, 92, 0.5) 75%,
    transparent 100%);
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.4s ease;
  z-index: -1;
  animation: border-rotate 4s linear infinite;
  background-size: 400% 400%;
  pointer-events: none;
}

@keyframes border-rotate {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.ship-card:hover::before {
  opacity: 0.6;
}

/* 卡片装饰 - 船舶图标 */
.ship-card::after {
  content: "🚢";
  position: absolute;
  bottom: 20px;
  right: 20px;
  font-size: 80px;
  opacity: 0.08;
  filter: grayscale(1) brightness(1.5);
  pointer-events: none;
  transition: all 0.4s ease;
  z-index: 0;
}

.ship-card:hover::after {
  opacity: 0.15;
  transform: scale(1.1) rotate(5deg);
  filter: grayscale(0.5) brightness(1.8);
}

/* 卡片内部发光效果 */
.ship-card > * {
  position: relative;
  z-index: 1;
}

/* 卡片悬停效果 */
.ship-card:hover {
  transform: translateY(-12px) scale(1.02);
  border-color: rgba(255, 214, 92, 0.6);
  box-shadow: 0 0 40px rgba(255, 214, 92, 0.4),
    0 0 70px rgba(255, 214, 92, 0.2),
    inset 0 0 30px rgba(255, 214, 92, 0.1);
  animation: card-glow-pulse 2s ease-in-out infinite;
}

@keyframes card-glow-pulse {
  0%, 100% {
    box-shadow: 0 0 40px rgba(255, 214, 92, 0.4),
      0 0 70px rgba(255, 214, 92, 0.2),
      inset 0 0 30px rgba(255, 214, 92, 0.1);
  }
  50% {
    box-shadow: 0 0 50px rgba(255, 214, 92, 0.6),
      0 0 90px rgba(255, 214, 92, 0.3),
      inset 0 0 40px rgba(255, 214, 92, 0.15);
  }
}

.ship-image {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(15, 43, 75, 0.8), rgba(6, 22, 39, 0.9));
  border-bottom: 2px solid rgba(255, 214, 92, 0.3);
}

/* 图片遮罩层 */
.ship-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(6, 22, 39, 0.7) 100%);
  pointer-events: none;
}

/* 图片扫描线效果 */
.ship-image::before {
  content: '';
  position: absolute;
  top: -100%;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(180deg, transparent, rgba(255, 214, 92, 0.3), transparent);
  animation: scan-line 4s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

@keyframes scan-line {
  0% { top: -100%; }
  50% { top: 100%; }
  100% { top: 100%; }
}

.ship-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease, filter 0.5s ease;
  filter: brightness(0.9) contrast(1.1) saturate(1.1);
}

.ship-card:hover .ship-image img {
  transform: scale(1.15) rotate(1deg);
  filter: brightness(1.1) contrast(1.3) saturate(1.3);
}

/* 图片边框光效 */
.ship-image {
  box-shadow: inset 0 0 30px rgba(255, 214, 92, 0.1);
  transition: box-shadow 0.4s ease;
}

.ship-card:hover .ship-image {
  box-shadow: inset 0 0 50px rgba(255, 214, 92, 0.2), inset 0 0 100px rgba(255, 183, 3, 0.1);
}

.ship-status {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  color: white;
  z-index: 2;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  animation: status-pulse 2s ease-in-out infinite;
  position: relative;
  overflow: hidden;
}

@keyframes status-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 状态标签闪光效果 */
.ship-status::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: status-shine 3s ease-in-out infinite;
}

@keyframes status-shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

.status-available {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.95), rgba(255, 183, 3, 0.95));
  border: 1px solid rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.6), 0 4px 15px rgba(0, 0, 0, 0.3);
  color: #051423;
}

.status-rented {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.95), rgba(255, 183, 3, 0.95));
  color: #051423;
  border: 1px solid rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.6), 0 4px 15px rgba(0, 0, 0, 0.3);
}

.status-maintenance {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.95), rgba(96, 98, 102, 0.95));
  border: 1px solid rgba(144, 147, 153, 0.5);
  box-shadow: 0 0 15px rgba(144, 147, 153, 0.4), 0 4px 15px rgba(0, 0, 0, 0.3);
}

.ship-info {
  position: relative;
  padding: 24px;
  z-index: 1;
}

/* 船舶信息装饰线 */
.ship-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffd65c, transparent);
  box-shadow: 0 0 8px rgba(255, 214, 92, 0.5);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.ship-card:hover .ship-info::before {
  opacity: 1;
}

.ship-info h3 {
  margin: 0 0 16px 0;
  font-size: 22px;
  font-weight: 800;
  color: #ffd65c;
  letter-spacing: 1px;
  text-shadow: 0 0 15px rgba(255, 214, 92, 0.6);
  position: relative;
  display: inline-block;
}

/* 标题下划线效果 */
.ship-info h3::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #ffd65c, #ffb703);
  box-shadow: 0 0 8px rgba(255, 214, 92, 0.6);
  transition: width 0.4s ease;
}

.ship-card:hover .ship-info h3::after {
  width: 100%;
}

.ship-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  position: relative;
}

/* 元数据区域装饰 */
.ship-meta::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 214, 92, 0.3), transparent);
}

.rating {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
  font-size: 16px;
  color: #ffd65c;
  text-shadow: 0 0 12px rgba(255, 214, 92, 0.6);
  animation: icon-pulse 3s ease-in-out infinite;
  position: relative;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(255, 214, 92, 0.1);
  border: 1px solid rgba(255, 214, 92, 0.3);
  transition: all 0.3s ease;
}

.rating:hover {
  background: rgba(255, 214, 92, 0.2);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.4);
  transform: scale(1.05);
}

@keyframes icon-pulse {
  0%, 100% {
    filter: drop-shadow(0 0 10px rgba(255, 214, 92, 0.6));
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(255, 214, 92, 0.9));
  }
}

.ship-price {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 12px;
  border-top: 1px solid rgba(255, 214, 92, 0.2);
  border-bottom: 1px solid rgba(255, 214, 92, 0.2);
  margin-bottom: 18px;
  background: linear-gradient(90deg, rgba(255, 214, 92, 0.05), rgba(255, 183, 3, 0.05));
  overflow: hidden;
}

/* 价格区域光效 */
.ship-price::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 214, 92, 0.2), transparent);
  transition: left 0.6s ease;
}

.ship-card:hover .ship-price::before {
  left: 100%;
}

.price-label {
  color: #e9f6ff;
  font-size: 14px;
  letter-spacing: 1px;
  position: relative;
  z-index: 1;
  font-weight: 600;
}

.price-value {
  font-size: 28px;
  font-weight: 800;
  color: #ffd65c;
  animation: value-glow 2s ease-in-out infinite;
  position: relative;
  z-index: 1;
  text-shadow: 0 0 20px rgba(255, 214, 92, 0.8), 0 0 40px rgba(255, 214, 92, 0.4);
}

/* 价格数字跳动效果 */
.ship-card:hover .price-value {
  animation: price-bounce 0.6s ease-in-out, value-glow 2s ease-in-out infinite;
}

@keyframes price-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.ship-actions {
  display: flex;
  gap: 10px;
  position: relative;
}

/* 操作按钮区域装饰 */
.ship-actions::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 214, 92, 0.3), transparent);
}

.ship-actions :deep(.el-button) {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: rgba(15, 43, 75, 0.8);
  border: 1px solid rgba(79, 168, 255, 0.4);
  color: var(--text-main);
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.ship-actions :deep(.el-button::before) {
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

.ship-actions :deep(.el-button:hover::before) {
  width: 300px;
  height: 300px;
}

.ship-actions :deep(.el-button:hover) {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9));
  color: #051423;
  border-color: transparent;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5), 0 4px 12px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
}

.ship-actions :deep(.el-button--danger) {
  background: rgba(75, 15, 43, 0.8);
  border-color: rgba(255, 90, 122, 0.4);
}

.ship-actions :deep(.el-button--danger::before) {
  background: rgba(255, 90, 122, 0.3);
}

.ship-actions :deep(.el-button--danger:hover) {
  background: linear-gradient(120deg, rgba(255, 90, 122, 0.9), rgba(255, 120, 150, 0.9));
  box-shadow: 0 0 20px rgba(255, 90, 122, 0.5), 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 按钮图标动画 */
.ship-actions :deep(.el-button:hover .el-icon) {
  animation: icon-bounce 0.6s ease-in-out;
}

@keyframes icon-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.pagination-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  padding: 28px;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 14px;
  box-shadow: 0 0 22px rgba(79, 168, 255, 0.15);
  backdrop-filter: blur(12px);
  overflow: hidden;
}

/* 分页器装饰 */
.pagination-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyan-400), var(--yellow-400), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
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
  color: var(--text-main);
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
  background: linear-gradient(90deg, transparent, var(--cyan-400), var(--yellow-400), transparent);
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
  background: linear-gradient(90deg, transparent, var(--cyan-400), var(--yellow-400), transparent);
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
  background: linear-gradient(90deg, var(--cyan-400), var(--yellow-400));
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
  color: var(--text-main);
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
  color: var(--text-main);
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

:deep(.maritime-dialog .el-input-group__append) {
  background: rgba(15, 43, 75, 0.8);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #ffd65c;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 0 8px rgba(255, 214, 92, 0.5);
  transition: all 0.3s ease;
  border-radius: 0 10px 10px 0;
  position: relative;
  overflow: hidden;
  min-height: 36px;
  display: flex;
  align-items: center;
  padding: 0 15px;
}

/* 单位后缀装饰 */
:deep(.maritime-dialog .el-input-group__append::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #ffd65c, #ffb703);
  box-shadow: 0 0 8px rgba(255, 214, 92, 0.6);
}

:deep(.maritime-dialog .el-input-group__append:hover) {
  background: rgba(15, 43, 75, 0.9);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.2);
  color: #ffb703;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.7);
}

/* 空状态样式 */
:deep(.el-empty) {
  position: relative;
  z-index: 1;
  padding: 80px 60px;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  box-shadow: 0 0 30px rgba(9, 35, 62, 0.6);
  overflow: hidden;
}

:deep(.el-empty::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(60, 235, 220, 0.1), transparent 70%);
  animation: empty-pulse 3s ease-in-out infinite;
}

@keyframes empty-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

:deep(.el-empty__image) {
  filter: drop-shadow(0 0 20px rgba(60, 235, 220, 0.3));
  animation: float-up-down 3s ease-in-out infinite;
}

@keyframes float-up-down {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

:deep(.el-empty__description) {
  color: var(--text-muted);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(60, 235, 220, 0.2);
}

/* 科技风按钮 */
.tech-btn-primary {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9)) !important;
  border: 1px solid rgba(255, 214, 92, 0.5) !important;
  color: #051423 !important;
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 1px;
  padding: 12px 24px;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.4), 0 4px 15px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.tech-btn-primary::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.tech-btn-primary:hover::before {
  width: 300px;
  height: 300px;
}

.tech-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 30px rgba(255, 214, 92, 0.6), 0 6px 20px rgba(0, 0, 0, 0.4);
}

.tech-btn-search {
  position: relative;
  overflow: hidden;
  background: rgba(15, 43, 75, 0.8) !important;
  border: 1px solid rgba(79, 168, 255, 0.4) !important;
  color: var(--text-main) !important;
  font-weight: 600;
  transition: all 0.3s ease;
}

.tech-btn-search::before {
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

.tech-btn-search:hover::before {
  width: 300px;
  height: 300px;
}

.tech-btn-search:hover {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9)) !important;
  color: #051423 !important;
  border-color: transparent !important;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
}

/* 分页器样式 */
:deep(.el-pagination) {
  display: flex;
  gap: 8px;
}

:deep(.el-pagination .el-pager li) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: var(--text-main);
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.8), rgba(255, 183, 3, 0.8));
  color: #051423;
  border-color: transparent;
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.5);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.9), rgba(255, 183, 3, 0.9));
  color: #051423;
  border-color: transparent;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.6);
}

:deep(.el-pagination button) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: var(--text-main);
  transition: all 0.3s ease;
}

:deep(.el-pagination button:hover:not(:disabled)) {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.8), rgba(255, 183, 3, 0.8));
  color: #051423;
  border-color: transparent;
}

:deep(.el-pagination .el-select .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
}

:deep(.el-pagination .el-select .el-input__inner) {
  color: var(--text-main);
}

/* 标签样式 */
:deep(.el-tag) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: var(--text-main);
  font-weight: 600;
  padding: 6px 12px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.el-tag::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.3), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tag:hover::before) {
  left: 100%;
}

:deep(.el-tag:hover) {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.8), rgba(255, 183, 3, 0.8));
  color: #051423;
  border-color: transparent;
  box-shadow: 0 0 15px rgba(255, 214, 92, 0.4);
  transform: scale(1.05);
}

/* MaritimeSelect 样式 */
:deep(.el-select) {
  position: relative;
  z-index: 100;
}

:deep(.el-select .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.2);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(60, 235, 220, 0.7);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1);
}

:deep(.el-select .el-input__inner) {
  color: var(--text-main);
  font-weight: 600;
}

:deep(.el-select .el-input__suffix) {
  color: var(--cyan-400);
}

/* 下拉菜单样式 */
:deep(.el-select-dropdown) {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%);
  border: 1px solid rgba(79, 168, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5), 0 0 40px rgba(60, 235, 220, 0.2);
  backdrop-filter: blur(20px);
  z-index: 9999;
  border-radius: 12px;
  overflow: hidden;
}

/* 下拉菜单装饰 */
:deep(.el-select-dropdown::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(255, 214, 92, 0.1), transparent 50%);
  pointer-events: none;
}

:deep(.el-select-dropdown__item) {
  color: var(--text-main);
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.el-select-dropdown__item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #ffd65c, #ffb703);
  transition: height 0.3s ease;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.6);
}

:deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(120deg, rgba(79, 168, 255, 0.3), rgba(255, 214, 92, 0.3));
  color: #ffd65c;
  text-shadow: 0 0 8px rgba(255, 214, 92, 0.5);
}

:deep(.el-select-dropdown__item:hover::before) {
  height: 80%;
}

:deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(120deg, rgba(79, 168, 255, 0.5), rgba(255, 214, 92, 0.5));
  color: #051423;
  font-weight: 800;
  box-shadow: inset 0 0 20px rgba(255, 214, 92, 0.2);
}

:deep(.el-select-dropdown__item.is-selected::before) {
  height: 100%;
  background: linear-gradient(180deg, #ffd65c, #ffb703);
}

/* 对话框按钮样式 */
:deep(.maritime-dialog .el-button--primary) {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.95), rgba(255, 183, 3, 0.95)) !important;
  border: 1px solid rgba(255, 214, 92, 0.5) !important;
  color: #051423 !important;
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 1px;
  padding: 12px 28px;
  min-height: 40px;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.4), 0 4px 15px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.tech-btn-primary::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.maritime-dialog .el-button--primary:hover::before) {
  width: 300px;
  height: 300px;
}

:deep(.maritime-dialog .el-button--primary:hover) {
  box-shadow: 0 0 30px rgba(255, 214, 92, 0.6), 0 6px 20px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--primary:active) {
  transform: translateY(0);
}

:deep(.maritime-dialog .el-button--default) {
  background: rgba(15, 43, 75, 0.6) !important;
  border: 1px solid rgba(79, 168, 255, 0.3) !important;
  color: var(--text-main) !important;
  font-weight: 600;
  padding: 12px 28px;
  min-height: 40px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.maritime-dialog .el-button--default::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 214, 92, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.maritime-dialog .el-button--default:hover::before) {
  width: 300px;
  height: 300px;
}

:deep(.maritime-dialog .el-button--default:hover) {
  background: linear-gradient(120deg, rgba(255, 214, 92, 0.8), rgba(255, 183, 3, 0.8)) !important;
  color: #051423 !important;
  border-color: transparent !important;
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.4);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--default:active) {
  transform: translateY(0);
}

/* 输入框聚焦效果 */
:deep(.maritime-dialog .el-input__wrapper.is-focus) {
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1) !important;
  border-color: rgba(60, 235, 220, 0.7) !important;
}

:deep(.maritime-dialog .el-textarea__inner:focus) {
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1) !important;
  border-color: rgba(60, 235, 220, 0.7) !important;
}

:deep(.maritime-dialog .el-select) {
  width: 100%;
}

:deep(.maritime-dialog .el-select .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.8);
  border: 1px solid rgba(79, 168, 255, 0.4);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
  min-height: 36px;
}

:deep(.maritime-dialog .el-select .el-input__wrapper:hover) {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.2);
  background: rgba(15, 43, 75, 0.9);
}

:deep(.maritime-dialog .el-select .el-input__wrapper.is-focus) {
  border-color: rgba(60, 235, 220, 0.7);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1);
}

:deep(.maritime-dialog .el-select .el-input__inner) {
  color: var(--text-main);
  font-weight: 500;
  line-height: 1.5;
}

:deep(.maritime-dialog .el-select .el-input__suffix) {
  color: #ffd65c;
  transition: all 0.3s ease;
}

:deep(.maritime-dialog .el-select .el-input__suffix:hover) {
  color: #ffb703;
  filter: drop-shadow(0 0 8px rgba(255, 214, 92, 0.6));
}

/* 对话框遮罩层 */
:deep(.el-overlay) {
  background: radial-gradient(circle at center, rgba(6, 22, 39, 0.85), rgba(6, 22, 39, 0.95)) !important;
  backdrop-filter: blur(12px) !important;
  animation: overlay-fade-in 0.3s ease-out;
}

@keyframes overlay-fade-in {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(12px);
  }
}

/* 对话框本身的背景 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
}

/* 对话框外层容器 */
:deep(.el-dialog__wrapper) {
  backdrop-filter: blur(8px);
}

/* 对话框关闭按钮 */
:deep(.maritime-dialog .el-dialog__close) {
  color: white !important;
  font-size: 22px;
  transition: all 0.3s ease;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

:deep(.maritime-dialog .el-dialog__close:hover) {
  color: var(--yellow-400) !important;
  transform: rotate(90deg) scale(1.1);
  filter: drop-shadow(0 0 15px rgba(255, 214, 92, 0.8));
  background: rgba(255, 214, 92, 0.2);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
}

/* 表单验证错误提示 */
:deep(.maritime-dialog .el-form-item__error) {
  color: #ff5a7a !important;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 0 0 8px rgba(255, 90, 122, 0.5);
  animation: error-shake 0.5s ease-in-out;
  padding-left: 0;
  margin-top: 8px !important;
  line-height: 1.5;
  position: static !important;
  display: block !important;
}

:deep(.maritime-dialog .el-form-item__error::before) {
  content: '⚠ ';
  font-size: 14px;
  animation: error-icon-pulse 1s ease-in-out infinite;
  display: inline-block;
  margin-right: 4px;
}

@keyframes error-icon-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

/* 加载状态按钮 */
:deep(.maritime-dialog .el-button.is-loading) {
  position: relative;
  overflow: hidden;
}

:deep(.maritime-dialog .el-button.is-loading::after) {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: button-loading 1.5s linear infinite;
}

@keyframes button-loading {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.6), rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.4);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.8), rgba(79, 168, 255, 0.8));
}

/* 加载动画 */
@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

.loading-shimmer {
  animation: shimmer 2s infinite;
  background: linear-gradient(90deg, rgba(60, 235, 220, 0.05) 0%, rgba(60, 235, 220, 0.15) 50%, rgba(60, 235, 220, 0.05) 100%);
  background-size: 1000px 100%;
}

/* 图标发光效果 */
:deep(.el-icon) {
  transition: all 0.3s ease;
}

:deep(.el-button:hover .el-icon) {
  filter: drop-shadow(0 0 8px currentColor);
  transform: scale(1.1);
}

/* 船舶网格进入动画 */
.ships-grid > .ship-card {
  animation: card-fade-in 0.6s ease-out backwards;
}

.ships-grid > .ship-card:nth-child(1) { animation-delay: 0.1s; }
.ships-grid > .ship-card:nth-child(2) { animation-delay: 0.2s; }
.ships-grid > .ship-card:nth-child(3) { animation-delay: 0.3s; }
.ships-grid > .ship-card:nth-child(4) { animation-delay: 0.4s; }
.ships-grid > .ship-card:nth-child(5) { animation-delay: 0.5s; }
.ships-grid > .ship-card:nth-child(6) { animation-delay: 0.6s; }

@keyframes card-fade-in {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 页面进入动画 */
.page-header {
  animation: header-slide-down 0.8s ease-out, header-glow 3s ease-in-out 1s infinite;
}

@keyframes header-slide-down {
  0% {
    opacity: 0;
    transform: translateY(-50px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes header-glow {
  0%, 100% {
    box-shadow: 0 0 30px rgba(9, 35, 62, 0.6), 0 0 60px rgba(60, 235, 220, 0.15);
  }
  50% {
    box-shadow: 0 0 40px rgba(9, 35, 62, 0.8), 0 0 80px rgba(60, 235, 220, 0.25);
  }
}

.filter-bar {
  animation: filter-slide-in 0.8s ease-out 0.2s backwards;
}

@keyframes filter-slide-in {
  0% {
    opacity: 0;
    transform: translateX(-50px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 页面底部装饰 */
.page-footer {
  position: relative;
  z-index: 1;
  margin-top: 40px;
  margin-bottom: 40px;
  padding: 7px 0;
}

.footer-decoration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.decoration-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyan-400), var(--yellow-400), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
  animation: line-glow 3s ease-in-out infinite;
}

@keyframes line-glow {
  0%, 100% {
    opacity: 0.5;
    box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
  }
  50% {
    opacity: 1;
    box-shadow: 0 0 20px rgba(60, 235, 220, 0.8);
  }
}

.decoration-text {
  color: #ffd65c;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  white-space: nowrap;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
  animation: text-glow 3s ease-in-out infinite;
}

@keyframes text-glow {
  0%, 100% {
    text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
  }
  50% {
    text-shadow: 0 0 20px rgba(255, 214, 92, 0.8);
  }
}

/* 船舶卡片内部光晕 */
.ship-card .ship-image,
.ship-card .ship-info {
  position: relative;
  z-index: 1;
}

.ship-card .ship-info {
  background: radial-gradient(ellipse at 20% 100%, rgba(60, 235, 220, 0.03), transparent 50%),
    radial-gradient(ellipse at 80% 100%, rgba(79, 168, 255, 0.03), transparent 50%);
  transition: background 0.4s ease;
}

.ship-card:hover .ship-info {
  background: radial-gradient(ellipse at 20% 100%, rgba(60, 235, 220, 0.08), transparent 50%),
    radial-gradient(ellipse at 80% 100%, rgba(79, 168, 255, 0.08), transparent 50%);
}

/* 鼠标悬停光标效果 */
.ship-card {
  cursor: pointer;
}

.ship-card:hover {
  cursor: pointer;
}

/* 添加点击波纹效果 */
.ship-card:active {
  transform: translateY(-10px) scale(0.98);
}

/* 响应式 */
@media (max-width: 768px) {
  .ships-page {
    padding: 20px;
  }

  .ships-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .filter-bar {
    flex-direction: column;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .ship-info h3 {
    font-size: 18px;
  }

  .price-value {
    font-size: 22px;
  }

  .stream-lines {
    display: none;
  }

  .page-header::after {
    display: none;
  }

  .decoration-text {
    font-size: 12px;
    letter-spacing: 2px;
  }

  .top-decoration {
    display: none;
  }

  .ship-card::before {
    display: none;
  }

  .ship-card:hover {
    transform: translateY(-8px);
  }
}

/* 打印样式 */
@media print {
  .stream-lines,
  .page-footer,
  .top-decoration {
    display: none !important;
  }

  .ship-card {
    box-shadow: none !important;
    border: 1px solid #ccc !important;
  }

  .ship-card::before,
  .ship-card::after {
    display: none !important;
  }
}

/* 性能优化 - 减少不必要的动画 */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .ships-page {
    background: radial-gradient(circle at 20% 20%, rgba(60, 235, 220, 0.1), transparent 40%),
      radial-gradient(circle at 80% 30%, rgba(79, 168, 255, 0.15), transparent 45%),
      linear-gradient(130deg, #030a14 0%, #061628 45%, #0a1e38 100%);
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: more) {
  .ship-card {
    border: 2px solid var(--cyan-400);
  }

  .ship-status {
    border: 2px solid currentColor;
  }

  .tech-btn-primary,
  .tech-btn-search {
    border: 2px solid currentColor;
  }
}

/* 浏览器兼容性 */
@supports not (backdrop-filter: blur(12px)) {
  .ship-card,
  .page-header,
  .filter-bar,
  .pagination-wrapper {
    background: rgba(9, 28, 52, 0.95);
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .ship-card:hover {
    transform: translateY(-8px) scale(1.01);
  }

  .ship-actions :deep(.el-button) {
    padding: 12px 16px;
    font-size: 16px;
  }
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 22, 39, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fade-in 0.3s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.loading-spinner {
  width: 80px;
  height: 80px;
  border: 4px solid rgba(60, 235, 220, 0.2);
  border-top-color: var(--cyan-400);
  border-radius: 50%;
  animation: spinner-rotate 1s linear infinite;
  box-shadow: 0 0 30px rgba(60, 235, 220, 0.5);
}

@keyframes spinner-rotate {
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-message {
  padding: 20px;
  background: rgba(255, 90, 122, 0.1);
  border: 1px solid rgba(255, 90, 122, 0.3);
  border-radius: 12px;
  color: var(--text-main);
  text-align: center;
  margin: 20px 0;
  animation: error-shake 0.5s ease-in-out;
}

@keyframes error-shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

/* 成功状态 */
.success-message {
  padding: 20px;
  background: rgba(60, 235, 220, 0.1);
  border: 1px solid rgba(60, 235, 220, 0.3);
  border-radius: 12px;
  color: var(--text-main);
  text-align: center;
  margin: 20px 0;
  animation: success-bounce 0.6s ease-in-out;
}

@keyframes success-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
</style>
