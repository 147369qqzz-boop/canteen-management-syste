<template>
  <div class="dishes-page">
    <div class="content-card">
      <!-- 顶部操作栏 -->
      <div class="toolbar">
        <span class="page-title">菜品管理</span>
        <div class="toolbar-actions">
          <el-input v-model="keyword" placeholder="搜索菜品名称..." clearable style="width: 200px" @input="currentPage = 1" />
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 110px" @change="currentPage = 1">
            <el-option label="全部" value="" />
            <el-option label="上架中" value="上架" />
            <el-option label="已下架" value="下架" />
          </el-select>
          <el-button-group>
            <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </el-button>
            <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </el-button>
          </el-button-group>
          <el-button type="primary" @click="openDialog()">
            <el-icon style="margin-right:4px"><Plus /></el-icon>新增菜品
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !filtered.length" class="empty-state">
        <div class="empty-icon">🍽️</div>
        <p class="empty-title">还没有菜品</p>
        <p class="empty-desc">点击右上角「新增菜品」开始添加您的菜单吧</p>
        <el-button type="primary" @click="openDialog()">新增菜品</el-button>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid' && filtered.length" v-loading="loading" class="grid-container">
        <div v-for="item in paged" :key="item.id" class="grid-card" @click="openDialog(item)">
          <div class="card-img">
            <el-image :src="item.img" fit="cover" style="width:100%;height:100%" />
            <el-tag size="small" :type="item.status === '上架' ? 'success' : 'danger'" round class="status-tag">
              {{ item.status }}
            </el-tag>
          </div>
          <div class="card-body">
            <h4 class="card-name">{{ item.name }}</h4>
            <div class="card-meta">
              <span class="card-price">¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <span class="card-store">库存 {{ item.store || 0 }}</span>
            </div>
            <div class="card-footer">
              <span class="card-sold">已售 {{ item.saleCount || 0 }}</span>
              <div class="card-actions" @click.stop>
                <el-button size="small" type="primary" @click="openDialog(item)">编辑</el-button>
                <el-popconfirm title="确定删除该菜品？" @confirm="handleDelete(item.id)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list' && filtered.length" v-loading="loading">
        <el-table :data="paged" stripe>
          <el-table-column label="图片" width="80">
            <template #default="{ row }">
              <el-image :src="row.img" style="width:48px;height:48px;border-radius:8px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column label="名称" prop="name" min-width="140" show-overflow-tooltip />
          <el-table-column label="价格" width="100" sortable>
            <template #default="{ row }">¥{{ Number(row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="库存" prop="store" width="80" />
          <el-table-column label="销量" prop="saleCount" width="80" />
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-switch :model-value="row.status === '上架'" @change="toggleStatus(row)"
                active-text="上架" inactive-text="下架"
                style="--el-switch-on-color:#13ce66;--el-switch-off-color:#ff4949" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div v-if="filtered.length > pageSize" class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="filtered.length"
          background layout="total, prev, pager, next" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜品' : '新增菜品'" width="540px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="菜品图片" prop="img">
          <el-upload :action="uploadUrl" :on-success="onImgUpload" :show-file-list="false" accept="image/*" drag>
            <div v-if="form.img">
              <el-image :src="form.img" style="width:120px;height:120px;border-radius:8px" fit="cover" />
              <span style="font-size:12px;color:#94a3b8">点击更换图片</span>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <span>点击或拖拽上传</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="库存" prop="store">
          <el-input-number v-model="form.store" :min="0" style="width:100%" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="菜品简介（选填）" />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="form.status" active-value="上架" inactive-value="下架" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { getLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { Grid, List, Plus } from '@element-plus/icons-vue'

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const viewMode = ref('grid')
const keyword = ref('')
const statusFilter = ref('')
const goods = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = 12

const businessId = ref(null)

const form = reactive({
  id: undefined, img: '', name: '', price: 0, store: 0, description: '', status: '上架'
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  store: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  img: [{ required: true, message: '请上传菜品图片', trigger: 'change' }]
}

const filtered = computed(() => {
  let list = goods.value
  if (keyword.value) list = list.filter(g => g.name && g.name.includes(keyword.value))
  if (statusFilter.value) list = list.filter(g => g.status === statusFilter.value)
  return list
})

const paged = computed(() => {
  const s = (currentPage.value - 1) * pageSize
  return filtered.value.slice(s, s + pageSize)
})

const fetchBusinessId = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  try {
    const r = await request.get('/business/info', { params: { userId: user.id } })
    businessId.value = r.data?.id || null
  } catch { /* ignore */ }
}

const fetchGoods = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loading.value = true
  try {
    const r = await request.get('/goods/business/goods', { params: { userId: user.id } })
    goods.value = r.data || []
  } catch {
    ElMessage.error('加载菜品失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === '上架' ? '下架' : '上架'
  try {
    await request.put('/goods/update', { id: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success('已' + newStatus)
  } catch {
    ElMessage.error('状态更新失败')
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, JSON.parse(JSON.stringify(row)))
    editingId.value = row.id
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

const resetForm = () => {
  editingId.value = null
  form.id = undefined
  form.img = ''
  form.name = ''
  form.price = 0
  form.store = 0
  form.description = ''
  form.status = '上架'
  formRef.value?.resetFields()
}

const onImgUpload = (res) => {
  if (res.code === '200') {
    form.img = res.data
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const isEdit = !!form.id
    const payload = { ...form, businessId: businessId.value }
    await request[isEdit ? 'put' : 'post'](isEdit ? '/goods/update' : '/goods/add', payload)
    ElMessage.success(isEdit ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await fetchGoods()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete('/goods/delete/' + id)
    ElMessage.success('已删除')
    await fetchGoods()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await fetchBusinessId()
  fetchGoods()
})
</script>

<style scoped>
.dishes-page { padding: 4px; }
.content-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}
.toolbar {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title { font-size: 18px; font-weight: 600; color: #1e293b; }
.toolbar-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.empty-state { text-align: center; padding: 60px 0; }
.empty-icon { font-size: 56px; margin-bottom: 12px; }
.empty-title { font-size: 16px; color: #64748b; margin: 0 0 6px; }
.empty-desc { font-size: 13px; color: #94a3b8; margin: 0 0 16px; }

.grid-container { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; padding: 16px; }
.grid-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.grid-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.card-img { position: relative; height: 160px; overflow: hidden; }
.status-tag { position: absolute; top: 8px; right: 8px; }
.card-body { padding: 14px; }
.card-name { margin: 0 0 8px; font-size: 15px; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.card-price { color: #f56c6c; font-weight: 700; font-size: 18px; }
.card-store { font-size: 12px; color: #94a3b8; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-sold { font-size: 12px; color: #94a3b8; }
.card-actions { display: flex; gap: 6px; }

.pagination-wrap { padding: 16px 24px; display: flex; justify-content: flex-end; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #c0c4cc; }
</style>
