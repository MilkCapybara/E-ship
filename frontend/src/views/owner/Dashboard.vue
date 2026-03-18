<template>
  <div class="dashboard-page">
    <!-- 数据卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="card-icon ship-icon">
          <el-icon><Ship /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ statistics.totalShips || 0 }}</div>
          <div class="card-label">船舶总数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon rented-icon">
          <el-icon><Checked /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ statistics.rentedShips || 0 }}</div>
          <div class="card-label">出租中</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon contract-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">{{ statistics.pendingContracts || 0 }}</div>
          <div class="card-label">待审核合约</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon income-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-value">¥{{ formatMoney(statistics.totalIncome) }}</div>
          <div class="card-label">总收入</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="chart-card">
        <div class="chart-header">
          <h3>合约状态分布</h3>
        </div>
        <div ref="contractStatusChart" class="chart"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>船舶类型分布</h3>
        </div>
        <div ref="shipTypeChart" class="chart"></div>
      </div>

      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>月度收入趋势</h3>
        </div>
        <div ref="monthlyIncomeChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Ship, Checked, Document, Money } from '@element-plus/icons-vue'
import { getOwnerStatistics } from '@/api/statistics'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import wsService from '@/utils/websocket'

const userStore = useUserStore()
const statistics = ref({})
const contractStatusChart = ref(null)
const shipTypeChart = ref(null)
const monthlyIncomeChart = ref(null)

const formatMoney = (value) => {
  if (!value) return '0'
  return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const loadStatistics = async () => {
  try {
    const res = await getOwnerStatistics(userStore.userInfo.id)
    statistics.value = res.data
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const initCharts = () => {
  initContractStatusChart()
  initShipTypeChart()
  initMonthlyIncomeChart()
}

const initContractStatusChart = () => {
  if (!contractStatusChart.value) return
  const chart = echarts.init(contractStatusChart.value)

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(6, 22, 39, 0.9)',
      borderColor: '#4fa8ff',
      textStyle: { color: '#e9f6ff' }
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#e9f6ff' }
    },
    series: [
      {
        name: '合约状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#061627',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
            color: '#ffd65c'
          }
        },
        labelLine: {
          show: false
        },
        data: statistics.value.contractStatusDistribution || [],
        color: ['#e6a23c', '#4fa8ff', '#67c23a', '#f56c6c']
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const initShipTypeChart = () => {
  if (!shipTypeChart.value) return
  const chart = echarts.init(shipTypeChart.value)

  const data = statistics.value.shipTypeDistribution || []
  const typeNames = {
    'CONTAINER': '集装箱船',
    'BULK': '散货船',
    'TANKER': '油船',
    'PASSENGER': '客船'
  }

  const formattedData = data.map(item => ({
    name: typeNames[item.name] || item.name,
    value: item.value
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(6, 22, 39, 0.9)',
      borderColor: '#4fa8ff',
      textStyle: { color: '#e9f6ff' }
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#e9f6ff' }
    },
    series: [
      {
        name: '船舶类型',
        type: 'pie',
        radius: '60%',
        data: formattedData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(255, 214, 92, 0.5)'
          }
        },
        color: ['#4fa8ff', '#ffd65c', '#67c23a', '#e6a23c']
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const initMonthlyIncomeChart = () => {
  if (!monthlyIncomeChart.value) return
  const chart = echarts.init(monthlyIncomeChart.value)

  const data = statistics.value.monthlyIncome || []
  const months = data.map(item => item.month)
  const amounts = data.map(item => item.amount)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(6, 22, 39, 0.9)',
      borderColor: '#4fa8ff',
      textStyle: { color: '#e9f6ff' },
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLine: { lineStyle: { color: '#4fa8ff' } },
      axisLabel: { color: '#e9f6ff' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#4fa8ff' } },
      axisLabel: { color: '#e9f6ff' },
      splitLine: { lineStyle: { color: 'rgba(79, 168, 255, 0.1)' } }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: amounts,
        smooth: true,
        lineStyle: {
          color: '#ffd65c',
          width: 3
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(255, 214, 92, 0.5)' },
              { offset: 1, color: 'rgba(255, 214, 92, 0.1)' }
            ]
          }
        },
        itemStyle: {
          color: '#ffd65c',
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// WebSocket消息处理
const handleWebSocketMessage = (notification) => {
  if (notification.type === 'CONTRACT_UPDATE') {
    const { action } = notification.data

    if (action === 'NEW_CONTRACT') {
      ElMessage.success('收到新的租赁申请！')
      loadStatistics() // 刷新统计数据
    } else if (action === 'CANCELLED') {
      ElMessage.info('租家取消了一个合约申请')
      loadStatistics() // 刷新统计数据
    }
  }
}

onMounted(() => {
  loadStatistics()

  // 连接WebSocket
  wsService.connect(userStore.userInfo.id)

  // 添加消息监听器
  wsService.addListener('owner-dashboard', handleWebSocketMessage)
})

onUnmounted(() => {
  // 移除消息监听器
  wsService.removeListener('owner-dashboard')
})
</script>

<style scoped>
.dashboard-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
  overflow-y: auto;
  max-height: calc(100vh - 60px);
}

/* 数据卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(6, 22, 39, 0.85) 0%, rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 214, 92, 0.1) 50%, transparent 100%);
  transition: left 0.6s ease;
}

.stat-card:hover::before {
  left: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: rgba(255, 214, 92, 0.5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4), 0 0 20px rgba(255, 214, 92, 0.3);
}

.card-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 28px;
}

.ship-icon {
  background: linear-gradient(135deg, rgba(79, 168, 255, 0.2), rgba(79, 168, 255, 0.3));
  color: #4fa8ff;
}

.rented-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.2), rgba(103, 194, 58, 0.3));
  color: #67c23a;
}

.contract-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.2), rgba(230, 162, 60, 0.3));
  color: #e6a23c;
}

.income-icon {
  background: linear-gradient(135deg, rgba(255, 214, 92, 0.2), rgba(255, 214, 92, 0.3));
  color: #ffd65c;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #ffd65c;
  margin-bottom: 4px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
}

.card-label {
  font-size: 14px;
  color: rgba(233, 246, 255, 0.7);
}

/* 图表容器 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: linear-gradient(135deg, rgba(6, 22, 39, 0.85) 0%, rgba(11, 31, 54, 0.85) 100%);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #ffd65c;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.3);
}

.chart {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }

  .chart-card.full-width {
    grid-column: 1;
  }
}

/* 滚动条样式 */
.dashboard-page::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.dashboard-page::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.dashboard-page::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.6), rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.4);
}

.dashboard-page::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.8), rgba(79, 168, 255, 0.8));
}
</style>
