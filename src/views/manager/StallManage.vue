<template>
  <div class="stall-manage-page">
    <div class="page-header">
      <h3>档口管理</h3>
      <div class="header-right">
        <el-select v-model="filterCanteenId" placeholder="按食堂筛选" clearable @change="loadData" style="width: 180px; margin-right: 12px">
          <el-option v-for="c in canteenList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="searchName" placeholder="搜索档口名称" clearable @clear="loadData" @keyup.enter="loadData" style="width: 200px" />
      </div>
    </div>

    <el-table :data="data.tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="businessName" label="档口名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column label="所属食堂" width="120">
        <template #default="{ row }">
          {{ getCanteenName(row.canteenId) }}
        </template>
      </el-table-column>
      <el-table-column prop="floor" label="楼层" width="70" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'warning'">
            {{ row.status ? '已通过' : '审核中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="营业状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.operateStatus === '营业中' ? 'success' : 'info'" size="small">
            {{ row.operateStatus || '营业中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(row)" v-if="!row.status">通过</el-button>
          <el-button size="small" @click="showAssign(row)">分配</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分配食堂对话框 -->
    <el-dialog v-model="assignVisible" title="分配档口到食堂" width="420px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="所属食堂">
          <el-select v-model="assignForm.canteenId" placeholder="选择食堂" style="width: 100%">
            <el-option v-for="c in canteenList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在楼层">
          <el-select v-model="assignForm.floor" placeholder="选择楼层" style="width: 100%">
            <el-option v-for="f in assignFloorList" :key="f" :label="f + '层'" :value="f" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const assignVisible = ref(false)
const canteenList = ref([])
const filterCanteenId = ref(null)
const searchName = ref('')

const data = reactive({ tableData: [] })
const assignForm = reactive({ id: null, canteenId: null, floor: null })

const assignFloorList = computed(() => {
  const c = canteenList.value.find(item => item.id === assignForm.canteenId)
  if (c && c.floors) {
    return c.floors.split(',').map(f => f.trim()).filter(f => f)
  }
  return []
})

const loadCanteens = async () => {
  const res = await request.get('/canteen/selectAll')
  canteenList.value = res.data || []
}

const getCanteenName = (id) => {
  const c = canteenList.value.find(item => item.id === id)
  return c ? c.name : '未分配'
}

const loadData = async () => {
  loading.value = true
  const params = {}
  if (filterCanteenId.value) params.canteenId = filterCanteenId.value
  if (searchName.value) params.businessName = searchName.value
  const res = await request.get('/business/selectAll', { params })
  data.tableData = res.data || []
  loading.value = false
}

const approve = async (row) => {
  await request.put('/business/review', { id: row.id, status: true })
  ElMessage.success('审核已通过')
  loadData()
}

const showAssign = (row) => {
  assignForm.id = row.id
  assignForm.canteenId = row.canteenId
  assignForm.floor = row.floor
  assignVisible.value = true
}

const confirmAssign = async () => {
  await request.put('/business/assignCanteen', assignForm)
  ElMessage.success('分配成功')
  assignVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该档口？', '确认', { type: 'warning' })
  // 删除档口需要删除对应的business记录
  await request.delete('/business/delete/' + id).catch(() => {})
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadCanteens()
  loadData()
})
</script>

<style scoped>
.stall-manage-page {
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
.header-right {
  display: flex;
  align-items: center;
}
</style>
