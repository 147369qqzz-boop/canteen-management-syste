<template>
  <div>
    <!-- 顶部统计卡片（保持不变） -->
    <div style="display: flex; grid-gap: 10px">
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px;">销售总额</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: red">￥{{ data.count.total }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px;">今日销售额</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #ff8200">￥{{ data.count.today }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px;">菜品总数</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #00b0ef">{{ data.count.goods }}</div>
      </div>
      <div class="card" style="padding: 20px; flex: 1; display: flex">
        <div style="flex: 1; font-size: 20px;">注册用户</div>
        <div style="flex: 1; font-size: 20px; font-weight: bold; color: #9b3cfd">{{ data.count.user }}</div>
      </div>
    </div>

    <!-- 第一行图表：折线图 + 饼图（原有） -->
    <div style="margin-top: 10px; display: flex; grid-gap: 10px">
      <div id="line" style="flex: 1; padding: 20px; height: 400px" class="card"></div>
      <div id="pie" style="flex: 1; padding: 20px; height: 400px" class="card"></div>
    </div>

    <!-- 第二行图表：菜品销量排行（柱状图） + 各时段订单分布（柱状图） -->
    <div style="margin-top: 10px; display: flex; grid-gap: 10px">
      <div id="rank" style="flex: 1; padding: 20px; height: 400px" class="card"></div>
      <div id="hour" style="flex: 1; padding: 20px; height: 400px" class="card"></div>
    </div>

  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request";
import * as echarts from 'echarts'

const data = reactive({
  count: {}
})

// ---------- 原有图表配置 ----------
const lineOption = {
  title: { text: '近一周订单销售的趋势图', left: 'center' },
  tooltip: { trigger: 'axis' },
  legend: { left: 'left' },
  xAxis: { name: '日期', type: 'category', data: [] },
  yAxis: { name: '销售额（元）', type: 'value' },
  grid: { top: '20%', bottom: '10%' },
  series: [{
    data: [],
    type: 'line',
    smooth: true,
    areaStyle: { opacity: 0.8, color: 'rgb(185,190,255)' },
    markPoint: { data: [{ type: 'max', name: 'Max' }, { type: 'min', name: 'Min' }] },
    markLine: { data: [{ type: 'average', name: 'Avg' }] }
  }]
}

const pieOption = {
  title: { text: '分类菜品销售额统计', subtext: '比例图', left: 'center' },
  tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c}元 ({d}%)' },
  legend: { top: 0, orient: 'vertical', left: 'left' },
  series: [{
    name: '销售额',
    type: 'pie',
    center: ['50%', '60%'],
    radius: '50%',
    data: [],
    label: { show: true, formatter(param) { return param.name + ' (' + param.percent + '%)'; } },
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
  }]
}

// ---------- 新增图表配置 ----------
// 1. 菜品销量排行（柱状图）
const rankOption = {
  title: { text: '菜品销量排行 TOP10', left: 'center' },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { top: '20%', bottom: '10%', left: '15%', right: '5%' },
  xAxis: {
    type: 'category',
    data: [],
    axisLabel: { rotate: 30, fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    name: '销量（份）'
  },
  series: [{
    data: [],
    type: 'bar',
    barWidth: '40%',
    itemStyle: {
      color: '#f38a5e',
      borderRadius: [8, 8, 0, 0]
    },
    label: {
      show: true,
      position: 'top',
      fontSize: 12
    }
  }]
}

// 2. 各时段订单分布（柱状图）
const hourOption = {
  title: { text: '各时段订单量分布', left: 'center' },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { top: '20%', bottom: '10%', left: '10%', right: '5%' },
  xAxis: {
    type: 'category',
    data: [],
    axisLabel: { rotate: 30 }
  },
  yAxis: {
    type: 'value',
    name: '订单数量'
  },
  series: [{
    data: [],
    type: 'bar',
    barWidth: '60%',
    itemStyle: {
      color: '#4a90e2',
      borderRadius: [8, 8, 0, 0]
    },
    label: {
      show: true,
      position: 'top',
      fontSize: 12
    }
  }]
}

// 获取顶部统计数据
request.get('/count').then(res => {
  data.count = res.data
})

// 等页面加载完成后渲染所有图表
onMounted(() => {
  // 1. 折线图
  const lineChart = echarts.init(document.getElementById('line'))
  request.get('/selectLine').then(res => {
    lineOption.xAxis.data = res.data.date
    lineOption.series[0].data = res.data.count
    lineChart.setOption(lineOption)
  })

  // 2. 饼图
  const pieChart = echarts.init(document.getElementById('pie'))
  request.get('/selectPie').then(res => {
    pieOption.series[0].data = res.data
    pieChart.setOption(pieOption)
  })

  // 3. 菜品销量排行（柱状图）
  const rankChart = echarts.init(document.getElementById('rank'))
  request.get('/selectGoodsRank', { params: { limit: 10 } }).then(res => {
    const names = res.data.map(item => item.name)
    const values = res.data.map(item => item.value)
    rankOption.xAxis.data = names
    rankOption.series[0].data = values
    rankChart.setOption(rankOption)
  })

  // 4. 各时段订单分布
  const hourChart = echarts.init(document.getElementById('hour'))
  request.get('/selectHourDistribution').then(res => {
    hourOption.xAxis.data = res.data.hours
    hourOption.series[0].data = res.data.counts
    hourChart.setOption(hourOption)
  })

  // 可选：窗口大小变化时自适应（非必须，但建议保留）
  window.addEventListener('resize', () => {
    lineChart.resize()
    pieChart.resize()
    rankChart.resize()
    hourChart.resize()
  })
})
</script>

<style scoped>
.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>