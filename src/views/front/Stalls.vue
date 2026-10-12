<template>
  <div class="stalls-container">
    <!-- 食堂选择 -->
    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">食堂：</span>
        <el-select v-model="data.canteenId" placeholder="选择食堂" clearable @change="onCanteenChange" style="width: 200px">
          <el-option v-for="c in data.canteenList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">楼层：</span>
        <el-select v-model="data.floor" placeholder="选择楼层" clearable @change="loadStalls" style="width: 160px">
          <el-option v-for="f in data.floorList" :key="f" :label="f + '层'" :value="f" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-tag v-if="data.filterStatus === 'all'" type="info" @click="data.filterStatus = '营业中'; loadStalls()">全部</el-tag>
        <el-tag v-else type="success" @click="data.filterStatus = 'all'; loadStalls()">仅营业中</el-tag>
      </div>
    </div>

    <!-- 档口列表 -->
    <div v-if="data.stalls.length > 0">
      <div class="stall-card" v-for="stall in data.stalls" :key="stall.id" @click="goStall(stall.id)">
        <div class="stall-header">
          <el-avatar :src="stall.avatar" :size="56" shape="square" style="border-radius: 12px" />
          <div class="stall-info">
            <div class="stall-name">
              {{ stall.businessName }}
              <el-tag :type="stall.operateStatus === '营业中' ? 'success' : 'danger'" size="small" style="margin-left: 8px">
                {{ stall.operateStatus || '营业中' }}
              </el-tag>
            </div>
            <div class="stall-address">{{ stall.address }} | {{ stall.floor || '未设置' }}层</div>
            <div class="stall-meta">
              <span>预计出餐：{{ stall.estimatedCookingTime || 10 }}分钟</span>
              <span style="margin-left: 16px">排队：{{ stall.queueCount || 0 }}人</span>
              <span style="margin-left: 16px">预计等待：{{ estimateWait(stall) }}分钟</span>
            </div>
          </div>
        </div>
        <!-- 菜品预览 -->
        <div class="goods-preview" v-if="stall.goodsList && stall.goodsList.length > 0">
          <div class="goods-mini-card" v-for="g in stall.goodsList.slice(0, 4)" :key="g.id" @click.stop="goGoodsDetail(g.id)">
            <img :src="g.img" alt="" />
            <div class="goods-mini-info">
              <div class="goods-mini-name">{{ g.name }}</div>
              <div class="goods-mini-price">¥{{ g.price }}</div>
              <div class="goods-mini-nutrition">
                <span>{{ g.calories || 0 }}千卡</span>
                <span>蛋白{{ g.protein || 0 }}g</span>
              </div>
              <div class="goods-mini-score" v-if="g.avgScore">
                <el-rate :model-value="g.avgScore" disabled show-score size="small" />
              </div>
            </div>
          </div>
        </div>
        <div class="stall-footer">
          <el-button type="primary" size="small" @click.stop="goStallGoods(stall.id)">查看全部菜品</el-button>
        </div>
      </div>
    </div>
    <div v-else style="text-align:center; padding: 60px 0">
      <el-empty description="暂无营业档口" />
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import router from '@/router'
import request from '@/utils/request'

const data = reactive({
  canteenId: null,
  floor: null,
  filterStatus: 'all',
  canteenList: [],
  floorList: [],
  stalls: [],
})

const loadCanteens = async () => {
  const res = await request.get('/canteen/selectAll')
  data.canteenList = res.data || []
}

const onCanteenChange = () => {
  data.floor = null
  const c = data.canteenList.find(item => item.id === data.canteenId)
  if (c && c.floors) {
    data.floorList = c.floors.split(',').map(f => f.trim()).filter(f => f)
  } else {
    data.floorList = []
  }
  loadStalls()
}

const loadStalls = async () => {
  const params = { filterStatus: data.filterStatus }
  if (data.canteenId) params.canteenId = data.canteenId
  if (data.floor) params.floor = data.floor
  const res = await request.get('/business/stallsDetail', { params })
  data.stalls = res.data || []
}

const estimateWait = (stall) => {
  const count = stall.queueCount || 0
  const perMeal = stall.estimatedCookingTime || 10
  return Math.ceil(count * perMeal / 5)
}

const goStall = (id) => { /* 查看档口详情 */ }
const goStallGoods = (id) => {
  router.push('/front/stallGoods?businessId=' + id)
}
const goGoodsDetail = (id) => {
  router.push('/front/goodsDetail?id=' + id)
}

onMounted(() => {
  loadCanteens()
  loadStalls()
})
</script>

<style scoped>
.stalls-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-weight: 600;
  color: #333;
}
.stall-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}
.stall-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.stall-header {
  display: flex;
  gap: 14px;
  align-items: center;
}
.stall-info {
  flex: 1;
}
.stall-name {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
}
.stall-address {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
}
.stall-meta {
  font-size: 13px;
  color: #64748b;
  margin-top: 6px;
}
.goods-preview {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.goods-mini-card {
  min-width: 160px;
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}
.goods-mini-card:hover {
  transform: scale(1.03);
}
.goods-mini-card img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}
.goods-mini-info {
  padding: 8px;
}
.goods-mini-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-mini-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
  margin-top: 2px;
}
.goods-mini-nutrition {
  font-size: 11px;
  color: #94a3b8;
  display: flex;
  gap: 6px;
  margin-top: 4px;
}
.goods-mini-score {
  margin-top: 4px;
}
.stall-footer {
  margin-top: 14px;
  text-align: right;
}
</style>
