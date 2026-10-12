<template>
  <div>
    <div class="card" style="margin-bottom: 10px; padding: 10px; background: #fff; border-radius: 4px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <div class="card" style="padding: 10px; background: #fff; border-radius: 4px;">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe border>
        <el-table-column label="用户名" prop="username" min-width="120"></el-table-column>
        <el-table-column label="名称" prop="name" min-width="120"></el-table-column>
        <el-table-column label="头像" align="center" width="80">
          <template #default="scope">
            <el-image
                :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                :preview-src-list="[scope.row.avatar]"
                preview-teleported
                style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover">
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role" min-width="100"></el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align: right; margin-top: 15px">
        <el-pagination
            @current-change="load"
            background
            layout="total,prev, pager, next"
            v-model:page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"/>
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog title="用户信息" width="400px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding-right: 20px">
        <el-form-item label="头像">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess" :show-file-list="false">
            <el-image v-if="data.form.avatar" :src="data.form.avatar" style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover"></el-image>
            <el-button v-else type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input :disabled="data.form.id != null" v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null
})

// 查询
const load = () => {
  request.get('/admin/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增
const add = () => {
  request.post('/admin/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('新增成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  })
}

// 更新
const update = () => {
  request.put('/admin/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('修改成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  })
}

// 保存
const save = () => {
  data.form.id ? update() : add()
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，确定删除？', '警告', { type: 'warning' })
      .then(() => {
        request.delete('/admin/delete/' + id).then(res => {
          if (res.code === '200') {
            load()
            ElMessage.success('删除成功')
          }
        })
      }).catch(() => {})
}

// 重置
const reset = () => {
  data.name = null
  load()
}

// 图片上传成功
const handleImgSuccess = (res) => {
  data.form.avatar = res.data
}

load()
</script>