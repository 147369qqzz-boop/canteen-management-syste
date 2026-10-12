<template>
  <div class="front-container">
    <!-- 公告栏 -->
    <div v-if="data.noticeList.length" class="notice-bar">
      <span class="notice-icon">📢</span>
      <div class="notice-scroll">
        <span v-for="item in data.noticeList" :key="item.id" class="notice-item">{{ item.title }}</span>
      </div>
    </div>
<!--    第一行开始  -->
    <div style="display: flex;grid-gap: 20px; margin-bottom: 40px">
      <div style="flex: 1">
      <el-carousel height="400px">
        <el-carousel-item v-for="item in data.carouselList" :key="item.id">
          <img @click="router.push('/front/carouselDetail?id='+ item.goodsId)" :src="item.img" alt="" style="width: 100%;height: 450px; cursor: pointer">
        </el-carousel-item>
      </el-carousel>
        </div>
        <div class="card" style="width: 260px">
              <div style="display: flex;align-items: center; padding-bottom: 5px; border-bottom: 1px solid red">
                <img src="@/assets/imgs/love.png" alt="" style="width: 25px">
                <div style="color: red; font-weight: bold; font-size: 20px;margin-left: 2px">为您推荐</div>
              </div>
          <div style="padding: 20px 0">
            <div class="top-item" @click="router.push('/front/goodsDetail?id='+ item.id)" style="cursor: pointer; display: flex; grid-gap: 10px; margin-bottom: 20px" v-for="item in data.recommendGoods" :key="item.id">
              <img style="width :65px;height: 65px" :src="item.img" alt="">
              <div>
                <div class="line2" style="margin-bottom: 5px">{{item.name}}</div>
                <div style="color: red"><span>¥</span><b>{{item.price}}</b> </div>
              </div>
            </div>
          </div>
        </div>
    </div>
    <!--    第一行结束  -->


    <div class="card" style="padding: 20px">
      <!--    第二行开始  -->
      <div style="display: flex;align-items: flex-end; padding-bottom: 5px; border-bottom: 1px solid red; margin-bottom: 10px">
        <div style="flex: 1; display: flex;align-items: center">
          <img src="@/assets/imgs/hot.png" alt="" style="width: 25px">
          <div style="color: red; font-weight: bold; font-size: 20px;margin-left: 2px">热销菜品</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer; font-size: 13px">查看更多</div>
      </div>
    <div style="margin-bottom: 40px">
      <el-row :gutter="20">
       <el-col :span="6" v-for="item in data.hotGoods" :key="item.id">
         <div @click="router.push('/front/goodsDetail?id='+ item.id)" class="item" style="width: 100%;border-radius: 5px;height: 350px" >
           <img :src="item.img" alt="" style="width: 100%; height: 260px; border-radius: 5px 5px 0 0">
           <div style="padding: 5px">
             <div class="line1" style="font-size: 16px">{{item.name}}</div>
             <div>
               <span style="color: red">¥</span><b style="color: red; font-size: 20px">{{item.price}}</b>
                <span style="margin-left: 10px; color: #666">销量：{{item.saleCount}}</span>
             </div>
           </div>
         </div>
       </el-col>
      </el-row>
    </div>
      <!--    第二行结束  -->



      <!--    第三行开始  -->
      <div style="display: flex;align-items: flex-end; padding-bottom: 5px; border-bottom: 1px solid red; margin-bottom: 10px">
        <div style="flex: 1; display: flex;align-items: center">
          <img src="@/assets/imgs/new.png" alt="" style="width: 25px">
          <div style="color: red; font-weight: bold; font-size: 20px;margin-left: 2px">最新上架</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer; font-size: 13px">查看更多</div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.newGoods" :key="item.id">
            <div @click="router.push('/front/goodsDetail?id='+ item.id)" class="item" style="width: 100%;border-radius: 5px;height: 350px" >
              <img :src="item.img" alt="" style="width: 100%; height: 230px; border-radius: 5px 5px 0 0">
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px">{{item.name}}</div>
                <div>
                  <span style="color: red">¥</span><b style="color: red; font-size: 20px">{{item.price}}</b>
                  <span style="margin-left: 10px; color: #666">销量：{{item.saleCount}}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
  </div>
    <!--    第三行结束  -->

  </div>
</template>

<script setup="">
import { reactive } from "vue";
import request from "@/utils/request";
import router from "@/router";

const data = reactive({
  carouselList: [],
  hotGoods: [],
  newGoods: [],
  recommendGoods: [],
  noticeList: [],
})

request.get('/notice/selectAll', { params: { status: 1 } }).then(res => {
  data.noticeList = res.data || []
})

request.get('/carousel/selectAll').then(res => {
  data.carouselList = res.data
})

request.get('/goods/selectAll',
    {
      params:{
        status: '上架'
      }
    }).then(res => {
  data.hotGoods = res.data.sort((v1,v2) => v2.saleCount - v1.saleCount).slice(0,4)
})

request.get('/goods/selectAll',
    {
      params:{
        status: '上架'
      }
    }).then(res => {
  data.newGoods = res.data.slice(0,4)
})

request.get('/goods/selectAll',
    {
      params:{
        status: '上架'
      }
    }).then(res => {
  data.recommendGoods = res.data.filter(v => v.recommend === '是').slice(0,5)
})
</script>

<style>
.notice-bar {
  background: linear-gradient(135deg, #fff7ed, #fffbeb);
  border: 1px solid #fde68a;
  border-radius: 10px;
  padding: 12px 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.notice-icon {
  font-size: 22px;
  flex-shrink: 0;
}
.notice-scroll {
  display: flex;
  gap: 32px;
  overflow: hidden;
  white-space: nowrap;
}
.notice-item {
  font-size: 14px;
  color: #92400e;
  position: relative;
}
.notice-item::after {
  content: '|';
  color: #d4d4d8;
  margin-left: 16px;
}
.notice-item:last-child::after {
  content: '';
}
.item{
  cursor: pointer;
}
.item:hover{
  border: 1px solid rgba(255, 0, 0, 0.73);
}
.top-item:hover{
  color: red;
}
</style>