<template>
  <div class="card">
    <!-- 头部欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-header">
        <h1>🍜 校园食堂 · 美味每一天</h1>
        <p class="subtitle">发现窗口新菜品，共享美好"食"光</p>
      </div>
      <div class="user-info" v-if="data.user.username">
        <span>👋 欢迎回来，{{ data.user.username }}</span>
      </div>
    </div>

    <!-- 今日推荐 / 特色窗口 -->
    <div class="feature-grid">
      <div class="feature-card">
        <div class="icon-wrapper">🍱</div>
        <h3>今日特供</h3>
        <p>红烧肉套餐 · 手工水饺 · 铁板意面</p>
        <span class="badge">限时供应</span>
      </div>
      <div class="feature-card">
        <div class="icon-wrapper">🥗</div>
        <h3>轻食沙拉</h3>
        <p>鸡胸藜麦沙拉 · 金枪鱼波奇饭</p>
        <span class="badge green">低卡专区</span>
      </div>
      <div class="feature-card">
        <div class="icon-wrapper">🍲</div>
        <h3>瓦罐煨汤</h3>
        <p>玉米排骨汤 · 茶树菇老鸭汤</p>
        <span class="badge orange">暖胃推荐</span>
      </div>
    </div>

    <!-- 食堂公告 / 开餐时间 -->
    <div class="info-panel">
      <div class="notice-board">
        <div class="notice-title">
          <span class="notice-icon">📢</span> 食堂公告
        </div>
        <ul v-if="data.noticeList.length">
          <li v-for="item in data.noticeList" :key="item.id">📢 {{ item.title }}</li>
        </ul>
        <ul v-else>
          <li>暂无公告</li>
        </ul>
      </div>
      <div class="time-board">
        <div class="time-title">
          <span class="time-icon">⏲️</span> 开餐时间
        </div>
        <ul>
          <li>早餐 06:30 - 09:00</li>
          <li>午餐 11:00 - 13:30</li>
          <li>晚餐 17:00 - 19:30</li>
          <li>夜宵 19:30 - 21:30</li>
        </ul>
      </div>
    </div>

    <!-- 食堂特色展示 -->
    <div class="gallery-section">
      <h2>📸 食堂掠影</h2>
      <div class="image-row">
        <div class="image-card">
          <div class="image-placeholder">🍛</div>
          <span>自选快餐</span>
        </div>
        <div class="image-card">
          <div class="image-placeholder">🍜</div>
          <span>风味面馆</span>
        </div>
        <div class="image-card">
          <div class="image-placeholder">🥘</div>
          <span>麻辣香锅</span>
        </div>
        <div class="image-card">
          <div class="image-placeholder">🧋</div>
          <span>奶茶烘焙</span>
        </div>
      </div>
    </div>

    <!-- 底部提示 -->
    <div class="footer-note">
      <p>🍚 一粥一饭当思来之不易，光盘行动从你我做起</p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeList: [],
})

request.get('/notice/selectAll', { params: { status: 1 } }).then(res => {
  data.noticeList = res.data || []
})
</script>

<style scoped>
.canteen-home {
  padding: 20px;
  background: #f8f5f0;
  min-height: 100vh;
  box-sizing: border-box;
  font-family: 'Segoe UI', 'PingFang SC', Roboto, sans-serif;
}

.welcome-card {
  background: linear-gradient(135deg, #ff9a44 0%, #fc6076 100%);
  border-radius: 24px;
  padding: 28px 30px;
  color: white;
  margin-bottom: 30px;
  box-shadow: 0 10px 25px rgba(252, 96, 118, 0.25);
}

.welcome-header h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.user-info {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  display: inline-block;
  padding: 6px 18px;
  border-radius: 40px;
  backdrop-filter: blur(5px);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f0e9e2;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.08);
}

.icon-wrapper {
  font-size: 42px;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 20px;
  margin: 0 0 10px 0;
  color: #3d2e1e;
}

.feature-card p {
  color: #6b5a4a;
  margin: 0 0 15px 0;
  font-size: 15px;
  line-height: 1.5;
}

.badge {
  background: #ffefe7;
  color: #d45a2e;
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
}

.badge.green {
  background: #e5f4e7;
  color: #2e7d5e;
}

.badge.orange {
  background: #fff1df;
  color: #c96f1d;
}

.info-panel {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.notice-board, .time-board {
  background: white;
  border-radius: 20px;
  padding: 20px 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0e9e2;
}

.notice-title, .time-title {
  font-size: 18px;
  font-weight: 600;
  color: #3d2e1e;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.notice-icon, .time-icon {
  font-size: 24px;
  margin-right: 10px;
}

.info-panel ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-panel li {
  padding: 8px 0;
  color: #5e4e3e;
  border-bottom: 1px dashed #ede5dc;
  font-size: 15px;
}

.info-panel li:last-child {
  border-bottom: none;
}

.gallery-section h2 {
  font-size: 22px;
  color: #3d2e1e;
  margin: 0 0 20px 0;
}

.image-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.image-card {
  background: white;
  border-radius: 20px;
  padding: 20px 15px 15px;
  flex: 1;
  min-width: 100px;
  text-align: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0e9e2;
  transition: all 0.2s;
}

.image-card:hover {
  border-color: #fcb38b;
  box-shadow: 0 10px 25px rgba(252, 96, 118, 0.1);
}

.image-placeholder {
  font-size: 48px;
  margin-bottom: 12px;
  background: #fff5e6;
  width: 80px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  border-radius: 50%;
  margin-left: auto;
  margin-right: auto;
}

.image-card span {
  font-weight: 500;
  color: #4d3a2b;
}

.footer-note {
  margin-top: 40px;
  text-align: center;
  color: #8f7a67;
  font-size: 15px;
  border-top: 1px solid #e7dfd6;
  padding-top: 24px;
}
</style>
