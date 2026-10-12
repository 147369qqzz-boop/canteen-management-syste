<template>
  <div class="feedback-page">
    <div class="card">
      <div style="font-size: 20px; font-weight: 600; color: #1e293b; margin-bottom: 24px">意见反馈</div>

      <!-- 提交反馈 -->
      <div class="submit-section">
        <div class="section-title">提交反馈</div>
        <el-form :model="data.form" ref="formRef" :rules="rules" label-width="0">
          <el-form-item prop="content">
            <el-input
              v-model="data.form.content"
              type="textarea"
              :rows="4"
              placeholder="请描述您的意见或建议，我们会认真对待每一条反馈..."
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback" :loading="data.submitting">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 历史反馈 -->
      <div class="history-section">
        <div class="section-title">我的反馈记录</div>
        <div v-if="!data.feedbackList.length && !data.loading" class="empty-state">
          <div class="empty-icon">📝</div>
          <p>暂无反馈记录</p>
        </div>
        <div v-else v-loading="data.loading">
          <div v-for="item in data.feedbackList" :key="item.id" class="feedback-item">
            <div class="feedback-header">
              <span class="feedback-time">{{ item.time }}</span>
              <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small" round>
                {{ item.status === 1 ? '已回复' : '待回复' }}
              </el-tag>
            </div>
            <p class="feedback-content">{{ item.content }}</p>
            <div v-if="item.reply" class="reply-box">
              <span class="reply-label">管理员回复：</span>
              <span class="reply-text">{{ item.reply }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const formRef = ref(null)
const user = JSON.parse(localStorage.getItem('system-user') || '{}')

const data = reactive({
  form: { content: '' },
  submitting: false,
  loading: false,
  feedbackList: []
})

const rules = {
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

const submitFeedback = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  data.submitting = true
  try {
    await request.post('/feedback/add', {
      userId: user.id,
      userName: user.name || user.username,
      content: data.form.content
    })
    ElMessage.success('感谢您的反馈！')
    data.form.content = ''
    loadFeedback()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    data.submitting = false
  }
}

const loadFeedback = () => {
  if (!user?.id) return
  data.loading = true
  request.get('/feedback/selectAll', {
    params: { userId: user.id }
  }).then(res => {
    data.feedbackList = res.data || []
  }).finally(() => {
    data.loading = false
  })
}

loadFeedback()
</script>

<style scoped>
.feedback-page {
  max-width: 780px;
  margin: 0 auto;
  padding: 20px 0;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #f56c6c;
}
.submit-section {
  margin-bottom: 36px;
}
.history-section {
  border-top: 1px solid #f1f5f9;
  padding-top: 24px;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #94a3b8;
}
.empty-icon { font-size: 48px; margin-bottom: 8px; }
.feedback-item {
  padding: 16px 0;
  border-bottom: 1px solid #f8fafc;
}
.feedback-item:last-child { border-bottom: none; }
.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.feedback-time { font-size: 13px; color: #94a3b8; }
.feedback-content {
  font-size: 15px;
  color: #334155;
  line-height: 1.6;
  margin: 0 0 8px;
}
.reply-box {
  background: #f0f9ff;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
}
.reply-label { color: #94a3b8; }
.reply-text { color: #0ea5e9; }
</style>
