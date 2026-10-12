<template>
  <div>
    <div class="card">
      <div style="font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 20px">意见反馈管理</div>

      <div style="margin-bottom: 16px">
        <el-radio-group v-model="data.statusFilter" @change="load">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="0">待回复</el-radio-button>
          <el-radio-button :value="1">已回复</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="data.tableData" stripe v-loading="data.loading">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="反馈用户" prop="userName" width="100" />
        <el-table-column label="反馈内容" prop="content" min-width="240" show-overflow-tooltip />
        <el-table-column label="回复内容" prop="reply" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :style="{ color: row.reply ? '#334155' : '#94a3b8' }">{{ row.reply || '暂无回复' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已回复' : '待回复' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="time" width="170" />
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.reply" type="primary" size="small" @click="openReply(row)">回复</el-button>
            <el-button v-if="row.reply" type="primary" size="small" link @click="openReply(row)">查看/编辑回复</el-button>
            <el-popconfirm title="确定删除该反馈？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          background
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="data.replyVisible" title="回复反馈" width="500px" destroy-on-close>
      <div style="margin-bottom: 16px">
        <div style="color: #94a3b8; font-size: 13px; margin-bottom: 4px">反馈内容</div>
        <div style="background: #f8fafc; padding: 12px; border-radius: 8px; color: #334155; line-height: 1.6">{{ data.currentFeedback?.content }}</div>
      </div>
      <el-form label-width="60px">
        <el-form-item label="回复">
          <el-input v-model="data.replyText" type="textarea" :rows="4" placeholder="请输入回复内容..." maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.replyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply" :loading="data.replying">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const data = reactive({
  tableData: [],
  loading: false,
  replying: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  statusFilter: undefined,
  replyVisible: false,
  replyText: '',
  currentFeedback: null
})

const load = () => {
  data.loading = true
  const params = { pageNum: data.pageNum, pageSize: data.pageSize }
  if (data.statusFilter !== undefined) params.status = data.statusFilter
  request.get('/feedback/selectPage', { params }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  }).finally(() => {
    data.loading = false
  })
}

const openReply = (row) => {
  data.currentFeedback = row
  data.replyText = row.reply || ''
  data.replyVisible = true
}

const handleReply = async () => {
  if (!data.replyText.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  data.replying = true
  try {
    await request.put('/feedback/reply', {
      id: data.currentFeedback.id,
      reply: data.replyText
    })
    ElMessage.success('回复成功')
    data.replyVisible = false
    load()
  } catch {
    ElMessage.error('回复失败')
  } finally {
    data.replying = false
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete('/feedback/delete/' + id)
    ElMessage.success('已删除')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}

load()
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>
