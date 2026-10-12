<template>
  <div class="business-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <el-icon :size="24"><Shop /></el-icon>
        </div>
        <span class="logo-text">商家中心</span>
      </div>

      <div class="user-info">
        <el-avatar :src="user.avatar" :size="48" />
        <div class="user-meta">
          <div class="user-name">{{ user.name || '商家用户' }}</div>
          <div class="user-role">商家</div>
        </div>
      </div>

      <el-menu
        router
        :default-active="route.path"
        class="menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
      >
        <el-menu-item index="/business/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/business/dishes">
          <el-icon><KnifeFork /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/business/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单处理</span>
        </el-menu-item>
        <el-menu-item index="/business/revenue">
          <el-icon><TrendCharts /></el-icon>
          <span>营收分析</span>
        </el-menu-item>
        <el-menu-item index="/business/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/business/status">
          <el-icon><Switch /></el-icon>
          <span>营业状态</span>
        </el-menu-item>
        <el-menu-item index="/business/settings">
          <el-icon><Setting /></el-icon>
          <span>店铺设置</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Shop, DataBoard, KnifeFork, Tickets, TrendCharts, ChatDotRound, Setting, SwitchButton
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getLoginUser, removeLoginUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const user = ref({})

onMounted(() => {
  const u = getLoginUser()
  if (!u?.id) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  user.value = u
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出当前商家账号吗？',
      '退出确认',
      { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning', center: true }
    )
    removeLoginUser()
    router.push('/login')
    ElMessage.success('已安全退出登录')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.business-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.user-meta {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: rgba(255,255,255,0.45);
  margin-top: 2px;
}

.menu {
  flex: 1;
  border: none !important;
  padding-top: 8px;
}

.menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 2px 12px;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
}

.menu .el-menu-item:hover {
  background: rgba(255,255,255,0.08) !important;
}

.menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  color: rgba(255,255,255,0.55);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(245, 108, 108, 0.15);
  color: #f56c6c;
}

.main-content {
  flex: 1;
  width: 0;
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
