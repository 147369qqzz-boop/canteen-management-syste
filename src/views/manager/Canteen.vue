<template>
  <div class="canteen-page">
    <div class="page-header">
      <h3>食堂管理</h3>
      <el-button type="primary" @click="showAdd">新增食堂</el-button>
    </div>

    <el-table :data="data.tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="食堂名称" />
      <el-table-column prop="location" label="位置描述" />
      <el-table-column prop="floors" label="楼层设置" />
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="showEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食堂' : '新增食堂'" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="食堂名称" required>
          <el-input v-model="form.name" placeholder="如：第一食堂" />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="form.location" placeholder="如：东校区生活区" />
        </el-form-item>
        <el-form-item label="楼层设置">
          <el-input v-model="form.floors" placeholder="如：1,2,3 （逗号分隔）" />
          <div class="form-hint">填写楼层编号，用逗号分隔，如：1,2,3</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const data = reactive({ tableData: [] })
const form = reactive({ id: null, name: '', location: '', floors: '' })

const loadData = async () => {
  loading.value = true
  const res = await request.get('/canteen/selectAll')
  data.tableData = res.data || []
  loading.value = false
}

const showAdd = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.location = ''
  form.floors = ''
  dialogVisible.value = true
}

const showEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const save = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入食堂名称')
    return
  }
  if (isEdit.value) {
    await request.put('/canteen/update', form)
  } else {
    await request.post('/canteen/add', form)
  }
  ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该食堂？', '确认', { type: 'warning' })
  await request.delete('/canteen/delete/' + id)
  ElMessage.success('删除成功')
  loadData()
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ')
}

onMounted(loadData)
</script>

<style scoped>
.canteen-page {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h3 {
  margin: 0;
  font-size: 18px;
}
.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
