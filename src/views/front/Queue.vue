<template>
  <div class="queue-container">
    <div class="page-header">
      <h2>实时排队情况</h2>
      <div class="header-actions">
        <el-tag type="success" effect="dark">
          <el-icon><Clock /></el-icon> {{ now }}
        </el-tag>
        <el-button size="small" @click="refresh" :loading="refreshing">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
      </div>
    </div>

    <!-- 食堂筛选 -->
    <div class="filter-row">
      <el-select v-model="data.canteenId" placeholder="筛选食堂" clearable @change="loadData" style="width: 200px">
        <el-option v-for="c in data.canteenList" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <!-- 排队卡片 -->
    <div v-if="data.stalls.length > 0">
      <el-row :gutter="16">
        <el-col :span="8" v-for="stall in sortedStalls" :key="stall.id">
          <div class="queue-card" :class="{ resting: stall.operateStatus === '休息中' }">
            <div class="q-header">
              <div class="q-name">
                {{ stall.businessName }}
                <el-tag :type="stall.operateStatus === '营业中' ? 'success' : 'info'" size="small">
                  {{ stall.operateStatus || '营业中' }}
                </el-tag>
              </div>
              <div class="q-floor" v-if="stall.floor">{{ stall.floor }}层</div>
            </div>
            <div class="q-body">
              <div class="q-stat">
                <div class="q-number" :class="{ 'text-danger': (stall.queueCount || 0) > 10 }">
                  {{ stall.queueCount || 0 }}
                </div>
                <div class="q-label">当前排队(人)</div>
              </div>
              <div class="q-stat">
                <div class="q-number">{{ estimateWait(stall) }}</div>
                <div class="q-label">预计等待(分钟)</div>
              </div>
              <div class="q-stat">
                <div class="q-number">{{ stall.estimatedCookingTime || 10 }}</div>
                <div class="q-label">出餐时间(分钟)</div>
              </div>
            </div>
            <div class="q-bar">
              <div class="bar-fill" :style="{ width: barWidth(stall) + '%' }" :class="barClass(stall)"></div>
            </div>
            <div class="q-hint">{{ queueHint(stall) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div v-else style="text-align:center; padding: 60px 0">
      <el-empty description="暂无营业档口" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'

const now = ref('')
const refreshing = ref(false)
const autoRefresh = ref(false)
let timer = null

const data = reactive({
  canteenId: null,
  canteenList: [],
  stalls: [],
})

const sortedStalls = computed(() => {
  return [...data.stalls].sort((a, b) => {
    if (a.operateStatus === '休息中' && b.operateStatus !== '休息中') return 1
    if (a.operateStatus !== '休息中' && b.operateStatus === '休息中') return -1
    return (b.queueCount || 0) - (a.queueCount || 0)
  })
})

const updateClock = () => {
  const d = new Date()
  now.value = d.toLocaleTimeString('zh-CN', { hour12: false })
}
updateClock()
setInterval(updateClock, 1000)

const loadCanteens = async () => {
  const res = await request.get('/canteen/selectAll')
  data.canteenList = res.data || []
}

const loadData = async () => {
  const params = {}
  if (data.canteenId) params.canteenId = data.canteenId
  const res = await request.get('/business/queueOverview', { params })
  data.stalls = res.data || []
}

const refresh = async () => {
  refreshing.value = true
  await loadData()
  refreshing.value = false
}

const estimateWait = (stall) => {
  const count = stall.queueCount || 0
  const perMeal = stall.estimatedCookingTime || 10
  return Math.ceil(count * perMeal / 5)
}

const barWidth = (stall) => {
  const wait = estimateWait(stall)
  return Math.min(100, wait * 5)
}

const barClass = (stall) => {
  const wait = estimateWait(stall)
  if (wait > 20) return 'bar-danger'
  if (wait > 10) return 'bar-warning'
  return 'bar-ok'
}

const queueHint = (stall) => {
  if (stall.operateStatus === '休息中') return '当前休息中'
  const count = stall.queueCount || 0
  if (count === 0) return '无需排队，可快速取餐'
  if (count < 5) return '排队较少，尽快取餐'
  if (count < 15) return '排队适中'
  return '排队较多，建议错峰'
}

onMounted(() => {
  loadCanteens()
  loadData()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.queue-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-row {
  margin-bottom: 20px;
}
.queue-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  transition: all 0.2s;
}
.queue-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}
.queue-card.resting {
  opacity: 0.65;
}
.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.q-name {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.q-floor {
  font-size: 12px;
  color: #94a3b8;
}
.q-body {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}
.q-stat {
  flex: 1;
  text-align: center;
}
.q-number {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
}
.q-number.text-danger {
  color: #f56c6c;
}
.q-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
.q-bar {
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}
.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s;
}
.bar-ok { background: #67c23a; }
.bar-warning { background: #e6a23c; }
.bar-danger { background: #f56c6c; }
.q-hint {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}
</style>
