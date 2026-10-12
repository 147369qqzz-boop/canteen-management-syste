<template>
  <div class="status-page">
    <div class="status-section">
      <div class="section-header">
        <span class="section-title">营业状态</span>
        <span class="section-desc">切换营业/休息状态，设置出餐时间，影响用户端排队计算</span>
      </div>
      <el-form label-width="120px" v-loading="loading" class="status-form">
        <el-form-item label="当前状态">
          <el-switch
            v-model="operating"
            active-text="营业中"
            inactive-text="休息中"
            :active-value="'营业中'"
            :inactive-value="'休息中'"
            size="large"
            @change="toggleStatus"
            :loading="toggling"
          />
        </el-form-item>
        <el-form-item label="预计出餐时间">
          <el-input-number v-model="cookingTime" :min="5" :max="60" :step="5" />
          <span class="unit-text">分钟/单</span>
          <div class="form-hint">出餐越快，排队预估等待时间越短，越能吸引学生</div>
        </el-form-item>
        <el-form-item label="当前排队人数">
          <el-input-number v-model="queueCount" :min="0" :max="200" />
          <span class="unit-text">人</span>
          <div class="form-hint">请根据实际排队情况更新，或由系统自动累积</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSettings" :loading="saving">保存设置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const user = getLoginUser()
const loading = ref(false)
const toggling = ref(false)
const saving = ref(false)
const operating = ref('营业中')
const cookingTime = ref(10)
const queueCount = ref(0)
const businessId = ref(null)

const loadStatus = async () => {
  loading.value = true
  try {
    const res = await request.get('/business/info', { params: { userId: user.id } })
    if (res.data) {
      businessId.value = res.data.id
      operating.value = res.data.operateStatus || '营业中'
      cookingTime.value = res.data.estimatedCookingTime || 10
      queueCount.value = res.data.queueCount || 0
    }
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

const toggleStatus = async () => {
  if (!businessId.value) return
  toggling.value = true
  try {
    await request.put('/business/toggleStatus', {
      id: businessId.value,
      operateStatus: operating.value,
    })
    ElMessage.success(operating.value === '营业中' ? '已切换为营业中' : '已切换为休息中')
  } catch {
    ElMessage.error('切换失败')
  } finally {
    toggling.value = false
  }
}

const saveSettings = async () => {
  if (!businessId.value) {
    ElMessage.warning('请先完善店铺信息')
    return
  }
  saving.value = true
  try {
    await request.put('/business/setCookingTime', {
      id: businessId.value,
      estimatedCookingTime: cookingTime.value,
    })
    await request.put('/business/updateQueue', {
      id: businessId.value,
      queueCount: queueCount.value,
    })
    ElMessage.success('设置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadStatus)
</script>

<style scoped>
.status-page {
  max-width: 600px;
}
.status-section {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  padding: 28px 32px;
}
.section-header {
  margin-bottom: 24px;
}
.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
}
.section-desc {
  font-size: 13px;
  color: #94a3b8;
  margin-left: 8px;
}
.status-form {
  max-width: 420px;
}
.unit-text {
  margin-left: 8px;
  color: #64748b;
  font-size: 13px;
}
.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
