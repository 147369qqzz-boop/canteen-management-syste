<template>
  <div>
    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
        <span style="font-size: 18px; font-weight: 600; color: #1e293b">公告管理</span>
        <el-button type="primary" @click="openDialog()">
          <el-icon style="margin-right:4px"><Plus /></el-icon>发布公告
        </el-button>
      </div>

      <el-table :data="data.tableData" stripe v-loading="data.loading">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
        <el-table-column label="内容" prop="content" min-width="280" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="time" width="170" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该公告？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="data.dialogVisible" :title="data.editingId ? '编辑公告' : '发布公告'" width="560px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="data.form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="data.form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="data.form.content" type="textarea" :rows="5" placeholder="请输入公告内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="data.form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="data.saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Plus } from '@element-plus/icons-vue'

const formRef = ref(null)

const data = reactive({
  tableData: [],
  loading: false,
  saving: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  dialogVisible: false,
  editingId: null,
  form: {
    title: '',
    content: '',
    status: 1
  }
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const load = () => {
  data.loading = true
  request.get('/notice/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  }).finally(() => {
    data.loading = false
  })
}

const openDialog = (row) => {
  if (row) {
    data.editingId = row.id
    data.form = {
      id: row.id,
      title: row.title,
      content: row.content,
      status: row.status
    }
  } else {
    resetForm()
  }
  data.dialogVisible = true
}

const resetForm = () => {
  data.editingId = null
  data.form = { title: '', content: '', status: 1 }
  formRef.value?.resetFields()
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  data.saving = true
  try {
    if (data.editingId) {
      await request.put('/notice/update', data.form)
      ElMessage.success('公告已更新')
    } else {
      await request.post('/notice/add', data.form)
      ElMessage.success('公告已发布')
    }
    data.dialogVisible = false
    load()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    data.saving = false
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete('/notice/delete/' + id)
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
