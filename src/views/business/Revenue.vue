<template>
  <div class="revenue-page">
    <div class="page-header">
      <span class="page-title">营收分析</span>
      <div class="date-actions">
        <el-radio-group v-model="dateRange" size="small" @change="onDateRangeChange">
          <el-radio-button value="7">近7天</el-radio-button>
          <el-radio-button value="15">近15天</el-radio-button>
          <el-radio-button value="30">近30天</el-radio-button>
        </el-radio-group>
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 营收概览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="8" v-for="item in overviewCards" :key="item.label">
        <div class="overview-card">
          <div class="overview-icon" :style="{ background: item.bg }">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="overview-body">
            <div class="overview-label">{{ item.label }}</div>
            <div class="overview-value">{{ item.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 营收趋势图 -->
      <el-col :span="14">
        <div class="chart-card">
          <div class="chart-header">营收趋势</div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>

      <!-- 菜品销量排行 -->
      <el-col :span="10">
        <div class="chart-card">
          <div class="chart-header">菜品销量排行</div>
          <div ref="rankChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- 分类占比 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">分类营收占比</div>
          <div ref="pieChartRef" class="chart-box" style="height:340px"></div>
        </div>
      </el-col>

      <!-- 时段分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">订单时段分布</div>
          <div ref="hourChartRef" class="chart-box" style="height:340px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { Refresh, Money, Tickets, KnifeFork } from '@element-plus/icons-vue'

const loading = ref(false)
const dateRange = ref('7')
const trendChartRef = ref(null)
const rankChartRef = ref(null)
const pieChartRef = ref(null)
const hourChartRef = ref(null)

const overviewCards = reactive([
  { label: '总营收', value: '¥0', icon: 'Money', bg: 'linear-gradient(135deg,#667eea,#764ba2)' },
  { label: '总订单数', value: '0', icon: 'Tickets', bg: 'linear-gradient(135deg,#f093fb,#f5576c)' },
  { label: '人均消费', value: '¥0', icon: 'KnifeFork', bg: 'linear-gradient(135deg,#43e97b,#38f9d7)' }
])

let ordersData = []

const refreshData = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loading.value = true
  try {
    // 获取订单数据
    const res = await request.get('/orders/business/orders', { params: { userId: user.id } })
    ordersData = (res.data || []).filter(o => o.status !== '已取消')

    // 概览统计
    const totalRevenue = ordersData.reduce((s, o) => s + (Number(o.total) || 0), 0)
    const totalOrders = ordersData.length
    overviewCards[0].value = '¥' + totalRevenue.toFixed(2)
    overviewCards[1].value = totalOrders
    overviewCards[2].value = totalOrders > 0 ? '¥' + (totalRevenue / totalOrders).toFixed(2) : '¥0'

    // 获取全局营收趋势 / 分类 / 时段数据
    const [trendRes, rankRes, pieRes, hourRes] = await Promise.allSettled([
      request.get('/selectLine'),
      request.get('/selectGoodsRank', { params: { limit: 10 } }),
      request.get('/selectPie'),
      request.get('/selectHourDistribution')
    ])

    nextTick(() => {
      // 营收趋势
      if (trendRes.status === 'fulfilled' && trendRes.value.data && trendChartRef.value) {
        const d = trendRes.value.data
        const chart = echarts.init(trendChartRef.value)
        chart.setOption({
          tooltip: { trigger: 'axis' },
          grid: { left: 55, right: 20, top: 20, bottom: 30 },
          xAxis: { type: 'category', data: d.date, axisLabel: { fontSize: 11 } },
          yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
          series: [{
            type: 'bar',
            data: d.count,
            itemStyle: {
              borderRadius: [6, 6, 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#667eea' }, { offset: 1, color: '#a78bfa' }
              ])
            }
          }]
        })
      }

      // 销量排行
      if (rankRes.status === 'fulfilled' && rankRes.value.data && rankChartRef.value) {
        const d = rankRes.value.data
        const chart = echarts.init(rankChartRef.value)
        chart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: 80, right: 30, top: 10, bottom: 20 },
          xAxis: { type: 'value' },
          yAxis: { type: 'category', data: d.map(x => x.name).reverse(), axisLabel: { fontSize: 11 } },
          series: [{
            type: 'bar',
            data: d.map(x => x.value).reverse(),
            itemStyle: {
              borderRadius: [0, 6, 6, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
              ])
            }
          }]
        })
      }

      // 分类占比
      if (pieRes.status === 'fulfilled' && pieRes.value.data && pieChartRef.value) {
        const d = pieRes.value.data
        const chart = echarts.init(pieChartRef.value)
        chart.setOption({
          tooltip: { trigger: 'item' },
          legend: { bottom: 0, textStyle: { fontSize: 12 } },
          series: [{
            type: 'pie',
            radius: ['50%', '75%'],
            center: ['50%', '45%'],
            data: d,
            label: { show: false },
            emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } }
          }]
        })
      }

      // 时段分布
      if (hourRes.status === 'fulfilled' && hourRes.value.data && hourChartRef.value) {
        const d = hourRes.value.data
        const chart = echarts.init(hourChartRef.value)
        chart.setOption({
          tooltip: { trigger: 'axis' },
          grid: { left: 45, right: 20, top: 20, bottom: 30 },
          xAxis: { type: 'category', data: d.hours, axisLabel: { rotate: 45, fontSize: 10 } },
          yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
          series: [{
            type: 'line',
            data: d.counts,
            smooth: true,
            symbol: 'circle',
            symbolSize: 4,
            lineStyle: { color: '#667eea', width: 2 },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102,126,234,0.25)' },
              { offset: 1, color: 'rgba(102,126,234,0.02)' }
            ])}
          }]
        })
      }
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onDateRangeChange = () => {
  // 日期切换时刷新数据
  refreshData()
}

onMounted(refreshData)
</script>

<style scoped>
.revenue-page { padding: 4px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 18px; font-weight: 600; color: #1e293b; }
.date-actions { display: flex; align-items: center; gap: 12px; }

.overview-row { margin-bottom: 16px; }

.overview-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}
.overview-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.overview-label { font-size: 13px; color: #94a3b8; margin-bottom: 4px; }
.overview-value { font-size: 24px; font-weight: 700; color: #1e293b; }

.chart-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}
.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}
.chart-box { width: 100%; height: 340px; }
</style>
