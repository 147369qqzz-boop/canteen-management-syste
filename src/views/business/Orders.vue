<template>
  <div class="orders-page">
    <!-- 状态概览卡片 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="6" v-for="s in summaryCards" :key="s.label">
        <div class="summary-card" :style="{ borderLeftColor: s.color }">
          <div class="summary-label">{{ s.label }}</div>
          <div class="summary-value">{{ s.count }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 订单列表 -->
    <div class="content-card">
      <div class="toolbar">
        <span class="page-title">订单处理</span>
        <div class="toolbar-right">
          <el-input v-model="searchOrderNo" placeholder="搜索订单号..." clearable style="width:220px" @keyup.enter="fetchOrders" />
          <el-button type="primary" @click="fetchOrders">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <!-- 状态选项卡 -->
      <div class="status-tabs">
        <span v-for="tab in statusTabs" :key="tab.key"
          :class="['tab-item', { active: statusFilter === tab.key }]"
          @click="statusFilter = tab.key; currentPage = 1">
          {{ tab.label }} <em>{{ tabCount(tab.key) }}</em>
        </span>
      </div>

      <!-- 订单表格 -->
      <el-table :data="pagedOrders" stripe v-loading="loading">
        <el-table-column label="订单号" prop="orderNo" min-width="190" show-overflow-tooltip />
        <el-table-column label="客户" prop="userName" width="90" />
        <el-table-column label="金额" width="100" sortable>
          <template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="配送方式" prop="deliverType" width="90" />
        <el-table-column label="下单时间" prop="time" width="170" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="dark" round size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待接单'" type="primary" size="small" @click="changeStatus(row, '制作中')">接单</el-button>
            <el-button v-if="row.status === '制作中'" type="success" size="small" @click="changeStatus(row, '已完成')">完成</el-button>
            <el-button v-if="row.status === '待接单'" type="danger" size="small" plain @click="changeStatus(row, '已取消')">拒单</el-button>
            <el-button size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredOrders.length > pageSize" class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="filteredOrders.length"
          background layout="total, prev, pager, next" />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="560px" destroy-on-close>
      <template v-if="currentOrder">
        <div class="order-no">{{ currentOrder.orderNo }}</div>
        <el-descriptions :column="1" border size="small" class="detail-desc">
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(currentOrder.status)">{{ currentOrder.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentOrder.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ Number(currentOrder.total || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.time }}</el-descriptions-item>
          <el-descriptions-item label="配送方式">{{ currentOrder.deliverType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="送达地址">{{ currentOrder.address || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder.orderDetailList?.length" class="detail-goods">
          <h4>商品明细</h4>
          <div v-for="item in currentOrder.orderDetailList" :key="item.goodsId" class="goods-item">
            <el-image :src="item.goodsImg" style="width:48px;height:48px;border-radius:8px" fit="cover" />
            <div class="goods-info">
              <div class="goods-name">{{ item.goodsName }}</div>
              <div class="goods-meta">¥{{ item.goodsPrice }} × {{ item.num }}</div>
            </div>
            <span class="goods-subtotal">¥{{ (item.goodsPrice * item.num).toFixed(2) }}</span>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('全部')
const currentPage = ref(1)
const pageSize = 10
const searchOrderNo = ref('')
const detailVisible = ref(false)
const currentOrder = ref(null)

const statusTabs = [
  { key: '全部' }, { key: '待接单' }, { key: '制作中' },
  { key: '已出货' }, { key: '已配送' }, { key: '已完成' }, { key: '已取消' }
]

const summaryCards = computed(() => {
  const c = (s) => orders.value.filter(o => o.status === s).length
  return [
    { label: '待接单', count: c('待接单'), color: '#f59e0b' },
    { label: '制作中', count: c('制作中'), color: '#3b82f6' },
    { label: '配送中', count: c('已配送') + c('已出货'), color: '#8b5cf6' },
    { label: '已完成', count: c('已完成'), color: '#10b981' }
  ]
})

const filteredOrders = computed(() => {
  return statusFilter.value === '全部'
    ? orders.value
    : orders.value.filter(o => o.status === statusFilter.value)
})

const pagedOrders = computed(() => {
  const s = (currentPage.value - 1) * pageSize
  return filteredOrders.value.slice(s, s + pageSize)
})

const tabCount = (key) => {
  return key === '全部' ? orders.value.length : orders.value.filter(o => o.status === key).length
}

const statusType = (s) => {
  const m = { '待接单': 'warning', '制作中': 'primary', '已完成': 'success', '已取消': 'danger' }
  return m[s] || 'info'
}

const fetchOrders = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loading.value = true
  try {
    const res = await request.get('/orders/business/orders', {
      params: { userId: user.id, orderNo: searchOrderNo.value || undefined }
    })
    orders.value = res.data || []
    currentPage.value = 1
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchOrderNo.value = ''
  statusFilter.value = '全部'
  fetchOrders()
}

const changeStatus = async (row, status) => {
  try {
    await request.put('/orders/update', { id: row.id, status })
    row.status = status
    ElMessage.success('状态已更新')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

const showDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

onMounted(fetchOrders)
</script>

<style scoped>
.orders-page { padding: 4px; }
.summary-row { margin-bottom: 20px; }

.summary-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  text-align: center;
  border-left: 4px solid;
}
.summary-label { font-size: 13px; color: #94a3b8; margin-bottom: 6px; }
.summary-value { font-size: 30px; font-weight: 700; color: #1e293b; }

.content-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}
.toolbar {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title { font-size: 18px; font-weight: 600; color: #1e293b; }
.toolbar-right { display: flex; align-items: center; gap: 8px; }

.status-tabs {
  padding: 12px 24px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  border-bottom: 1px solid #f1f5f9;
}
.tab-item {
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #64748b;
  transition: all 0.2s;
  user-select: none;
}
.tab-item em { font-style: normal; margin-left: 4px; opacity: 0.7; }
.tab-item.active { background: #6366f1; color: #fff; border-color: #6366f1; }
.tab-item:hover:not(.active) { border-color: #cbd5e1; }

.pagination-wrap { padding: 16px 24px; display: flex; justify-content: flex-end; }

.order-no { color: #94a3b8; font-size: 13px; margin-bottom: 16px; word-break: break-all; }
.detail-desc { margin-bottom: 20px; }

.detail-goods h4 { margin: 0 0 12px; font-size: 15px; color: #1e293b; }
.goods-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}
.goods-info { flex: 1; }
.goods-name { font-size: 14px; color: #334155; }
.goods-meta { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.goods-subtotal { font-weight: 600; color: #f56c6c; }
</style>
