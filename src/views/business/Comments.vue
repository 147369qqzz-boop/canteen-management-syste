<template>
  <div class="reviews-page">
    <div class="content-card">
      <div class="page-header">
        <span class="page-title">评价管理</span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !comments.length" class="empty-state">
        <div class="empty-icon">💬</div>
        <p class="empty-title">暂无评价</p>
        <p class="empty-desc">用户购买您的菜品后，评价会显示在这里</p>
      </div>

      <template v-else>
        <!-- 评分概览 -->
        <div class="rating-overview">
          <div class="rating-score">
            <span class="score-num">{{ avgScore.toFixed(1) }}</span>
            <span class="score-total">/ 5</span>
            <el-rate :model-value="avgScore" disabled text-color="#f59e0b" size="small" />
            <span class="score-count">共 {{ comments.length }} 条评价</span>
          </div>
          <div class="rating-bars">
            <div v-for="star in 5" :key="star" class="bar-row">
              <span class="bar-label">{{ 6 - star }} 星</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: starPct(6 - star) + '%' }"></div>
              </div>
              <span class="bar-count">{{ starCount(6 - star) }}</span>
            </div>
          </div>
        </div>

        <!-- 评价列表 -->
        <div v-loading="loading" class="review-list">
          <div v-for="item in comments" :key="item.id" class="review-item">
            <el-avatar :src="item.userAvatar" :size="44" class="review-avatar" />
            <div class="review-body">
              <div class="review-header">
                <span class="review-username">{{ item.userName }}</span>
                <el-rate :model-value="item.score" disabled text-color="#f59e0b" size="small" />
                <span class="review-time">{{ item.time }}</span>
              </div>
              <p class="review-content">{{ item.content || '无文字评价' }}</p>
              <!-- 已回复 -->
              <div v-if="item.reply" class="reply-box">
                <span class="reply-label">我的回复：</span>
                <span class="reply-text">{{ item.reply }}</span>
              </div>
              <!-- 未回复 - 回复输入 -->
              <div v-else class="reply-input-row">
                <el-input v-model="replyTexts[item.id]" placeholder="输入回复内容..." size="small" style="width:260px" />
                <el-button size="small" type="primary" @click="submitReply(item)" :loading="replying[item.id]">回复</el-button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const replyTexts = reactive({})
const replying = reactive({})

const avgScore = computed(() => {
  if (!comments.value.length) return 0
  return comments.value.reduce((s, c) => s + (c.score || 0), 0) / comments.value.length
})

const starCount = (s) => comments.value.filter(c => c.score === s).length
const starPct = (s) => comments.value.length ? Math.round(starCount(s) / comments.value.length * 100) : 0

const fetchComments = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loading.value = true
  try {
    const res = await request.get('/comment/business/comments', { params: { userId: user.id } })
    comments.value = res.data || []
    comments.value.forEach(c => { if (!replyTexts[c.id]) replyTexts[c.id] = '' })
  } catch {
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const submitReply = async (row) => {
  if (!replyTexts[row.id]?.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying[row.id] = true
  try {
    await request.put('/comment/reply', { id: row.id, reply: replyTexts[row.id] })
    row.reply = replyTexts[row.id]
    replyTexts[row.id] = ''
    ElMessage.success('回复成功')
  } catch {
    ElMessage.error('回复失败')
  } finally {
    replying[row.id] = false
  }
}

onMounted(fetchComments)
</script>

<style scoped>
.reviews-page { padding: 4px; }

.content-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}
.page-header {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.page-title { font-size: 18px; font-weight: 600; color: #1e293b; }

.empty-state { text-align: center; padding: 60px 0; }
.empty-icon { font-size: 56px; margin-bottom: 12px; }
.empty-title { font-size: 16px; color: #64748b; margin: 0 0 6px; }
.empty-desc { font-size: 13px; color: #94a3b8; margin: 0; }

.rating-overview {
  display: flex;
  align-items: center;
  gap: 48px;
  padding: 24px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}
.rating-score {
  text-align: center;
  min-width: 120px;
}
.score-num { font-size: 48px; font-weight: 800; color: #f59e0b; line-height: 1; }
.score-total { font-size: 14px; color: #94a3b8; }
.score-count { font-size: 12px; color: #94a3b8; display: block; margin-top: 4px; }
.rating-bars { flex: 1; max-width: 360px; }
.bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.bar-label { font-size: 13px; color: #64748b; width: 32px; }
.bar-track { flex: 1; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #fbbf24, #f59e0b); border-radius: 4px; transition: width 0.4s; }
.bar-count { font-size: 13px; color: #94a3b8; width: 20px; text-align: right; }

.review-list { padding: 0; }
.review-item {
  display: flex;
  gap: 14px;
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.review-avatar { flex-shrink: 0; }
.review-body { flex: 1; min-width: 0; }
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.review-username { font-weight: 600; font-size: 14px; color: #1e293b; }
.review-time { font-size: 12px; color: #94a3b8; margin-left: auto; }
.review-content { margin: 0 0 8px; font-size: 14px; color: #475569; line-height: 1.6; }

.reply-box { background: #f0f9ff; padding: 10px 14px; border-radius: 10px; font-size: 13px; }
.reply-label { color: #94a3b8; }
.reply-text { color: #0ea5e9; }

.reply-input-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
</style>
