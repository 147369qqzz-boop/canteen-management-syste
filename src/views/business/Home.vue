<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-text">
        <h2>欢迎回来，{{ business.businessName || '商家用户' }} 👋</h2>
        <p>祝您今天生意兴隆，订单不断！</p>
      </div>
      <div class="banner-date">{{ today }}</div>
    </div>

    <!-- 系统公告 -->
    <div v-if="notices.length" class="notice-row">
      <span class="notice-tag">📢 系统公告</span>
      <span v-for="n in notices" :key="n.id" class="notice-text">{{ n.title }}</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-tip">{{ card.tip }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 近7天营收趋势 -->
      <el-col :span="14">
        <div class="panel">
          <div class="panel-header">
            <span>近7天营收趋势</span>
          </div>
          <div ref="trendChart" class="chart-box"></div>
        </div>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="10">
        <div class="panel">
          <div class="panel-header">
            <span>快捷操作</span>
          </div>
          <div class="shortcut-list">
            <div v-for="item in shortcuts" :key="item.path" class="shortcut-item" @click="$router.push(item.path)">
              <div class="shortcut-icon" :style="{ background: item.bg }">
                <el-icon :size="20"><component :is="item.icon" /></el-icon>
              </div>
              <div class="shortcut-info">
                <div class="shortcut-label">{{ item.label }}</div>
                <div class="shortcut-desc">{{ item.desc }}</div>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近订单 -->
    <div class="panel" style="margin-top: 20px">
      <div class="panel-header">
        <span>最近订单</span>
        <el-button type="primary" link @click="$router.push('/business/orders')">查看全部</el-button>
      </div>
      <el-table :data="recentOrders" stripe v-loading="loading" empty-text="暂无订单">
        <el-table-column label="订单号" prop="orderNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="客户" prop="userName" width="100" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="时间" prop="time" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" round>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ArrowRight } from '@element-plus/icons-vue'

const business = ref({})
const loading = ref(false)
const recentOrders = ref([])
const notices = ref([])
const trendChart = ref(null)

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const statCards = reactive([
  { label: '今日订单', value: 0, tip: '较昨日 --', icon: 'Tickets', bg: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { label: '今日营收', value: '¥0', tip: '较昨日 --', icon: 'Money', bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { label: '在售菜品', value: 0, tip: '共上架菜品', icon: 'KnifeFork', bg: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { label: '好评率', value: '0%', tip: '用户好评率', icon: 'Star', bg: 'linear-gradient(135deg, #43e97b, #38f9d7)' }
])

const shortcuts = [
  { label: '菜品管理', desc: '添加或编辑菜品信息', icon: 'KnifeFork', path: '/business/dishes', bg: '#eff6ff' },
  { label: '订单处理', desc: '查看和处理订单', icon: 'Tickets', path: '/business/orders', bg: '#f0fdf4' },
  { label: '营收分析', desc: '查看营收数据报表', icon: 'TrendCharts', path: '/business/revenue', bg: '#fefce8' },
  { label: '评价管理', desc: '查看和回复用户评价', icon: 'ChatDotRound', path: '/business/reviews', bg: '#fdf2f8' },
  { label: '店铺设置', desc: '完善店铺资料信息', icon: 'Setting', path: '/business/settings', bg: '#f8fafc' }
]

const statusTag = (s) => {
  const m = { '待接单': 'warning', '制作中': 'primary', '已完成': 'success', '已取消': 'danger' }
  return m[s] || 'info'
}

const loadData = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loading.value = true
  try {
    const [biz, orders, goods, comments] = await Promise.allSettled([
      request.get('/business/info', { params: { userId: user.id } }),
      request.get('/orders/business/orders', { params: { userId: user.id } }),
      request.get('/goods/business/goods', { params: { userId: user.id } }),
      request.get('/comment/business/comments', { params: { userId: user.id } })
    ])

    if (biz.status === 'fulfilled' && biz.value.data) business.value = biz.value.data

    const o = orders.status === 'fulfilled' ? (orders.value.data || []) : []
    const g = goods.status === 'fulfilled' ? (goods.value.data || []) : []
    const c = comments.status === 'fulfilled' ? (comments.value.data || []) : []

    const todayStr = new Date().toISOString().slice(0, 10)
    const todayOrders = o.filter(x => x.time && x.time.includes(todayStr))
    const todayRevenue = todayOrders.filter(x => x.status !== '已取消').reduce((s, x) => s + (Number(x.total) || 0), 0)

    statCards[0].value = todayOrders.length
    statCards[1].value = '¥' + todayRevenue.toFixed(2)
    statCards[2].value = g.filter(x => x.status === '上架').length
    const totalScore = c.reduce((s, x) => s + (x.score || 0), 0)
    statCards[3].value = c.length ? Math.round(totalScore / c.length * 20) + '%' : '100%'

    recentOrders.value = o.slice(0, 5)

    // 渲染趋势图 (用全部订单按日期聚合)
    nextTick(() => {
      if (!trendChart.value) return
      const chart = echarts.init(trendChart.value)
      const days = []
      const values = []
      for (let i = 6; i >= 0; i--) {
        const d = new Date(Date.now() - i * 86400000)
        days.push((d.getMonth() + 1) + '/' + d.getDate())
        const ds = d.toISOString().slice(0, 10)
        const total = o.filter(x => x.status !== '已取消' && x.time && x.time.includes(ds))
          .reduce((s, x) => s + (Number(x.total) || 0), 0)
        values.push(total)
      }
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 20, bottom: 30 },
        xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#e2e8f0' } } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
        series: [{
          type: 'line',
          data: values,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#667eea', width: 3 },
          itemStyle: { color: '#667eea' },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102,126,234,0.3)' },
            { offset: 1, color: 'rgba(102,126,234,0.02)' }
          ])}
        }]
      })
      window.addEventListener('resize', () => chart.resize())
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadNotices = async () => {
  try {
    const r = await request.get('/notice/selectAll', { params: { status: 1 } })
    notices.value = r.data || []
  } catch { /* ignore */ }
}

onMounted(() => { loadData(); loadNotices() })
</script>

<style scoped>
.dashboard { padding: 4px; }

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 36px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.welcome-banner h2 { margin: 0 0 6px; font-size: 22px; font-weight: 700; }
.welcome-banner p { margin: 0; opacity: 0.8; font-size: 14px; }
.banner-date { font-size: 14px; opacity: 0.75; }

.notice-row {
  background: #fff;
  border-radius: 12px;
  padding: 14px 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  overflow: hidden;
}
.notice-tag {
  font-size: 13px;
  font-weight: 600;
  color: #f59e0b;
  flex-shrink: 0;
}
.notice-text {
  font-size: 13px;
  color: #475569;
  white-space: nowrap;
}

.stat-row { margin-bottom: 20px; }

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.stat-icon { width: 50px; height: 50px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.stat-label { font-size: 13px; color: #94a3b8; margin-bottom: 4px; }
.stat-value { font-size: 26px; font-weight: 700; color: #1e293b; }
.stat-tip { font-size: 12px; color: #cbd5e1; margin-top: 2px; }

.panel {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}
.panel-header {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.chart-box { width: 100%; height: 320px; }

.shortcut-list { padding: 8px 16px 16px; }
.shortcut-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.shortcut-item:hover { background: #f8fafc; }
.shortcut-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #475569; flex-shrink: 0;
}
.shortcut-info { flex: 1; }
.shortcut-label { font-size: 14px; font-weight: 600; color: #334155; }
.shortcut-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; }
</style>
