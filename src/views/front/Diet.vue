<template>
  <div class="diet-container">
    <!-- 目标设定卡片 -->
    <div class="goal-card">
      <div class="card-title">
        饮食目标
        <el-button size="small" text @click="showGoalDialog = true">编辑</el-button>
      </div>
      <div v-if="data.goal" class="goal-info">
        <el-tag :type="goalTypeTag" size="large">{{ data.goal.goalType || '保持' }}</el-tag>
        <div class="goal-numbers">
          <div class="goal-item">
            <span class="goal-label">每日热量</span>
            <span class="goal-value">{{ data.goal.dailyCalories || 2000 }}<small>千卡</small></span>
          </div>
          <div class="goal-item">
            <span class="goal-label">蛋白质</span>
            <span class="goal-value">{{ data.goal.dailyProtein || 60 }}<small>g</small></span>
          </div>
          <div class="goal-item">
            <span class="goal-label">碳水</span>
            <span class="goal-value">{{ data.goal.dailyCarbs || 250 }}<small>g</small></span>
          </div>
          <div class="goal-item">
            <span class="goal-label">脂肪</span>
            <span class="goal-value">{{ data.goal.dailyFat || 65 }}<small>g</small></span>
          </div>
        </div>
      </div>
      <div v-else class="goal-empty" @click="showGoalDialog = true">
        <el-empty description="未设置饮食目标，点击设置" :image-size="60" />
      </div>
    </div>

    <!-- 今日摄入概览 -->
    <div class="today-card">
      <div class="card-title">今日摄入概览</div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in todaySummary" :key="item.label">
          <div class="stat-box" :class="{ over: item.over }">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.current }} / {{ item.target }}{{ item.unit }}</div>
            <el-progress :percentage="item.percent" :color="item.over ? '#f56c6c' : '#67c23a'" :stroke-width="8" />
            <div v-if="item.over" class="over-warning">
              <el-icon><WarningFilled /></el-icon> 已超标！
            </div>
          </div>
        </el-col>
      </el-row>
      <!-- 健康建议 -->
      <div v-if="hasWarning" class="health-tips">
        <div class="tips-title"><el-icon><InfoFilled /></el-icon> 健康建议</div>
        <ul>
          <li v-if="caloriesOver">今日热量已超标，建议减少高热量食物摄入，增加蔬菜比例。</li>
          <li v-if="proteinOver">蛋白质摄入偏高，可适当减少肉类，增加蔬果。</li>
          <li v-if="carbsOver">碳水摄入偏高，建议用粗粮替代精米白面。</li>
          <li v-if="fatOver">脂肪摄入偏高，减少油炸食品和肥肉。</li>
          <li v-if="!hasWarning">各项指标均在目标范围内，继续保持！</li>
        </ul>
      </div>
    </div>

    <!-- 餐次记录 -->
    <div class="records-card">
      <div class="card-title">
        饮食记录
        <el-button size="small" type="primary" @click="showRecordDialog = true">添加记录</el-button>
      </div>
      <el-tabs v-model="data.activeMeal" @tab-change="loadRecords">
        <el-tab-pane label="早餐" name="早餐" />
        <el-tab-pane label="午餐" name="午餐" />
        <el-tab-pane label="晚餐" name="晚餐" />
        <el-tab-pane label="加餐" name="加餐" />
      </el-tabs>
      <el-table :data="data.records" stripe style="width: 100%">
        <el-table-column prop="goodsName" label="菜品名称" />
        <el-table-column prop="calories" label="热量(千卡)" width="100" />
        <el-table-column prop="protein" label="蛋白质(g)" width="100" />
        <el-table-column prop="carbs" label="碳水(g)" width="90" />
        <el-table-column prop="fat" label="脂肪(g)" width="90" />
        <el-table-column prop="recordTime" label="时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.recordTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click="deleteRecord(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 历史趋势 -->
    <div class="trend-card">
      <div class="card-title">近7天摄入趋势</div>
      <div ref="chartRef" style="width: 100%; height: 300px"></div>
    </div>

    <!-- 目标编辑对话框 -->
    <el-dialog v-model="showGoalDialog" title="饮食目标设置" width="480px">
      <el-form :model="goalForm" label-width="120px">
        <el-form-item label="目标类型">
          <el-radio-group v-model="goalForm.goalType">
            <el-radio value="减脂">减脂</el-radio>
            <el-radio value="增肌">增肌</el-radio>
            <el-radio value="保持">保持</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="每日热量(千卡)">
          <el-input-number v-model="goalForm.dailyCalories" :min="500" :max="5000" :step="50" />
        </el-form-item>
        <el-form-item label="蛋白质(g)">
          <el-input-number v-model="goalForm.dailyProtein" :min="10" :max="300" :step="5" />
        </el-form-item>
        <el-form-item label="碳水(g)">
          <el-input-number v-model="goalForm.dailyCarbs" :min="50" :max="600" :step="10" />
        </el-form-item>
        <el-form-item label="脂肪(g)">
          <el-input-number v-model="goalForm.dailyFat" :min="10" :max="200" :step="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGoalDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGoal">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加饮食记录对话框 -->
    <el-dialog v-model="showRecordDialog" title="添加饮食记录" width="480px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="菜品">
          <el-select v-model="recordForm.goodsId" filterable placeholder="搜索菜品" @change="onGoodsSelect" style="width: 100%">
            <el-option v-for="g in goodsOptions" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="recordForm.mealType" style="width: 100%">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </el-form-item>
        <el-form-item label="热量(千卡)">
          <el-input-number v-model="recordForm.calories" :min="0" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="蛋白质(g)">
          <el-input-number v-model="recordForm.protein" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="碳水(g)">
          <el-input-number v-model="recordForm.carbs" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="脂肪(g)">
          <el-input-number v-model="recordForm.fat" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordDialog = false">取消</el-button>
        <el-button type="primary" @click="addRecord">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const user = getLoginUser()
const showGoalDialog = ref(false)
const showRecordDialog = ref(false)
const chartRef = ref(null)
const goodsOptions = ref([])

const data = reactive({
  goal: null,
  records: [],
  allTodayRecords: [],
  activeMeal: '早餐',
})

const goalForm = reactive({
  id: null,
  userId: user?.id,
  goalType: '保持',
  dailyCalories: 2000,
  dailyProtein: 60,
  dailyCarbs: 250,
  dailyFat: 65,
})

const recordForm = reactive({
  userId: user?.id,
  goodsId: null,
  goodsName: '',
  mealType: '午餐',
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0,
})

const goalTypeTag = computed(() => {
  const map = { '减脂': 'danger', '增肌': 'success', '保持': 'info' }
  return map[data.goal?.goalType] || 'info'
})

const loadGoal = async () => {
  const res = await request.get('/dietGoal/selectAll', { params: { userId: user.id } })
  if (res.data && res.data.length > 0) {
    data.goal = res.data[0]
    Object.assign(goalForm, data.goal)
  }
}

const loadRecords = async () => {
  const today = new Date().toISOString().split('T')[0]
  // 加载当前餐次
  const res = await request.get('/dietRecord/selectAll', {
    params: { userId: user.id, mealType: data.activeMeal }
  })
  data.records = (res.data || []).filter(r => {
    return r.recordTime && r.recordTime.includes(today)
  })
  // 加载全部今日记录用于汇总
  const all = await request.get('/dietRecord/selectAll', {
    params: { userId: user.id }
  })
  data.allTodayRecords = (all.data || []).filter(r => {
    return r.recordTime && r.recordTime.includes(today)
  })
}

const todaySummary = computed(() => {
  const goal = data.goal || { dailyCalories: 2000, dailyProtein: 60, dailyCarbs: 250, dailyFat: 65 }
  let totalCal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0
  for (const r of data.allTodayRecords) {
    totalCal += Number(r.calories || 0)
    totalProtein += Number(r.protein || 0)
    totalCarbs += Number(r.carbs || 0)
    totalFat += Number(r.fat || 0)
  }
  return [
    { label: '热量', current: totalCal, target: Number(goal.dailyCalories), unit: '千卡', percent: Math.min(100, Math.round(totalCal / Number(goal.dailyCalories) * 100)), over: totalCal > Number(goal.dailyCalories) },
    { label: '蛋白质', current: totalProtein, target: Number(goal.dailyProtein), unit: 'g', percent: Math.min(100, Math.round(totalProtein / Number(goal.dailyProtein) * 100)), over: totalProtein > Number(goal.dailyProtein) },
    { label: '碳水', current: totalCarbs, target: Number(goal.dailyCarbs), unit: 'g', percent: Math.min(100, Math.round(totalCarbs / Number(goal.dailyCarbs) * 100)), over: totalCarbs > Number(goal.dailyCarbs) },
    { label: '脂肪', current: totalFat, target: Number(goal.dailyFat), unit: 'g', percent: Math.min(100, Math.round(totalFat / Number(goal.dailyFat) * 100)), over: totalFat > Number(goal.dailyFat) },
  ]
})

const caloriesOver = computed(() => todaySummary.value[0].over)
const proteinOver = computed(() => todaySummary.value[1].over)
const carbsOver = computed(() => todaySummary.value[2].over)
const fatOver = computed(() => todaySummary.value[3].over)
const hasWarning = computed(() => caloriesOver.value || proteinOver.value || carbsOver.value || fatOver.value)

const saveGoal = async () => {
  goalForm.userId = user.id
  if (goalForm.id) {
    await request.put('/dietGoal/update', goalForm)
  } else {
    await request.post('/dietGoal/add', goalForm)
  }
  ElMessage.success('目标已保存')
  showGoalDialog.value = false
  loadGoal()
}

const addRecord = async () => {
  if (!recordForm.goodsId) {
    ElMessage.warning('请选择菜品')
    return
  }
  await request.post('/dietRecord/add', recordForm)
  ElMessage.success('记录已添加')
  showRecordDialog.value = false
  loadRecords()
}

const deleteRecord = async (id) => {
  await request.delete('/dietRecord/delete/' + id)
  ElMessage.success('已删除')
  loadRecords()
}

const onGoodsSelect = (val) => {
  const g = goodsOptions.value.find(item => item.id === val)
  if (g) {
    recordForm.goodsName = g.name
    recordForm.calories = g.calories || 0
    recordForm.protein = g.protein || 0
    recordForm.carbs = g.carbs || 0
    recordForm.fat = g.fat || 0
  }
}

const loadGoodsOptions = async () => {
  const res = await request.get('/goods/selectAll')
  goodsOptions.value = res.data || []
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ')
}

const loadChart = async () => {
  // Load all records for last 7 days
  const res = await request.get('/dietRecord/selectAll', { params: { userId: user.id } })
  const allRecords = res.data || []
  const days = []
  const calData = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    days.push(dateStr.substring(5))
    let sum = 0
    for (const r of allRecords) {
      if (r.recordTime && r.recordTime.includes(dateStr)) {
        sum += Number(r.calories || 0)
      }
    }
    calData.push(sum)
  }
  await nextTick()
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: days },
      yAxis: { type: 'value', name: '千卡' },
      series: [{ data: calData, type: 'line', smooth: true, areaStyle: {}, name: '热量摄入' }],
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
    })
  }
}

onMounted(() => {
  loadGoal()
  loadRecords()
  loadGoodsOptions()
  loadChart()
})
</script>

<style scoped>
.diet-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}
.goal-card, .today-card, .records-card, .trend-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.goal-info {
  display: flex;
  align-items: center;
  gap: 24px;
}
.goal-numbers {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.goal-item {
  text-align: center;
}
.goal-label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
}
.goal-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}
.goal-value small {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
}
.goal-empty {
  cursor: pointer;
  padding: 10px 0;
}
.stat-box {
  text-align: center;
  padding: 12px;
}
.stat-label { font-size: 13px; color: #64748b; }
.stat-value { font-size: 15px; font-weight: 600; color: #1e293b; margin: 6px 0 8px; }
.stat-box.over .stat-value { color: #f56c6c; }
.over-warning {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.health-tips {
  margin-top: 20px;
  padding: 14px 16px;
  background: #fefce8;
  border-radius: 10px;
  border-left: 4px solid #eab308;
}
.tips-title {
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.health-tips ul {
  margin: 0;
  padding-left: 20px;
}
.health-tips li {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}
</style>
