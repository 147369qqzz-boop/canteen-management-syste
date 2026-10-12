<template>
  <div class="stall-goods-container">
    <div class="page-header">
      <el-button @click="router.back()" :icon="'ArrowLeft'" text>返回档口列表</el-button>
      <h2>{{ stallName }} - 全部菜品</h2>
    </div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in data.goodsList" :key="item.id">
        <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="card goods-item">
          <img :src="item.img" alt="" />
          <div class="goods-info">
            <div class="goods-name">{{ item.name }}</div>
            <div class="goods-nutrition">
              <el-tag size="small" type="warning">{{ item.calories || 0 }}千卡</el-tag>
              <el-tag size="small" type="success">蛋白{{ item.protein || 0 }}g</el-tag>
              <el-tag size="small">碳水{{ item.carbs || 0 }}g</el-tag>
            </div>
            <div class="goods-bottom">
              <span class="goods-price">¥{{ item.price }}</span>
              <span class="goods-sale">销量{{ item.saleCount }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import request from '@/utils/request'

const route = useRoute()
const stallName = ref('')
const data = reactive({ goodsList: [] })

onMounted(async () => {
  const businessId = route.query.businessId
  if (businessId) {
    const res = await request.get('/goods/selectAll', { params: { businessId } })
    data.goodsList = res.data || []
    // 获取档口名
    const bRes = await request.get('/business/selectAll', { params: { id: businessId } })
    stallName.value = bRes.data?.[0]?.businessName || '档口'
  }
})
</script>

<style scoped>
.stall-goods-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.goods-item {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  margin-bottom: 20px;
  transition: transform 0.3s;
}
.goods-item:hover { transform: scale(1.05); }
.goods-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.goods-info { padding: 10px; }
.goods-name { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.goods-nutrition { display: flex; gap: 4px; margin-bottom: 8px; flex-wrap: wrap; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; }
.goods-price { color: #f56c6c; font-size: 18px; font-weight: 700; }
.goods-sale { color: #999; font-size: 12px; }
</style>
