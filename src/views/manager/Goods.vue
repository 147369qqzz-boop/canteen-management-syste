<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-select style="width: 300px; margin-right: 10px " v-model="data.categoryId" placeholder="请选择菜系">
        <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table  tooltip-effect="dark myEff" :data="data.tableData" stripe>
        <el-table-column prop="name" label="名称" show-overflow-tooltip></el-table-column>
        <el-table-column label="图片">
          <template #default="scope">
            <el-image v-if="scope.row.img" :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported style="width: 100px; height: 100px;"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格">
          <template #default="scope">
            <b style="color: red">{{ scope.row.price }}元</b>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="简介" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="详情">
          <template #default="scope">
            <el-button type="primary" @click="view(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="store" label="库存"></el-table-column>
        <el-table-column prop="cuisineName" label="菜系"></el-table-column>
        <el-table-column prop="status" label="上架状态">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待审核'">待审核</el-tag>
            <el-tag type="success" v-if="scope.row.status === '通过'">通过</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '拒绝'">拒绝</el-tag>
            <span v-else>{{ scope.row.status || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="推荐">
          <template #default="scope">
            <el-switch @change="updateRecommed(scope.row)" style="--el-switch-on-color: #13ce66" v-model="scope.row.recommendActive"
                       active-text="是" inactive-text="否"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量"></el-table-column>
        <el-table-column prop="saleCount" label="销量"></el-table-column>
        <el-table-column prop="time" label="创建时间"></el-table-column>

        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total,prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="菜品信息" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px;padding-top: 20px">
        <el-form-item prop="name" label="名称">
          <el-input v-model="data.form.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="图片">
          <!-- 核心修改：添加文件大小校验、限制和超出处理 -->
          <el-upload
              :action="uploadUrl"
              list-type="picture"
              :on-success="handleImgSuccess"
              :before-upload="beforeUpload"
          :on-exceed="handleExceed"
          :limit="1"
          :file-list="fileList"
          :on-remove="handleRemove"
          >
          <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="price" label="价格">
          <el-input-number min="0" v-model="data.form.price" placeholder="请输入价格"></el-input-number>
        </el-form-item>
        <el-form-item prop="description" label="简介">
          <el-input type="textarea" :rows="3" v-model="data.form.description" placeholder="请输入简介"></el-input>
        </el-form-item>

        <el-form-item prop="store" label="库存">
          <el-input v-model="data.form.store" placeholder="请输入库存"></el-input>
        </el-form-item>
        <el-form-item prop="categoryId" label="菜系">
          <el-select style="width: 100%" v-model="data.form.categoryId">
            <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="status" label="上架状态">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="上架" value="上架"></el-radio-button>
            <el-radio-button label="下架" value="下架"></el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="content" label="详情">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 500px; overflow-y: hidden;"
                v-model="data.form.content"
                :mode="mode"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="菜系介绍" v-model="data.viewVisible" width="50%" :close-on-click-modal="false" destroy-on-close>
      <div class="editor-content-view" style="padding: 20px" v-html="data.content"></div>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="data.viewVisible = false">关 闭</el-button>
    </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, watch } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, shallowRef } from "vue";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const formRef = ref()
//绑定上传文件列表，用于回显和清空
const fileList = ref([])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  categoryList: [],
  name: null,
  categoryId: null,
  viewVisible: false,
  content: null,
  rules: {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' }
    ],
    img: [
      { required: true, message: '请上传图片', trigger: 'change' } // 修正触发方式为change
    ],
    price: [
      { required: true, message: '请输入价格', trigger: 'blur' }
    ],
    description: [
      { required: true, message: '请输入简介', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入详情', trigger: 'blur' }
    ],
    store: [
      { required: true, message: '请输入库存', trigger: 'blur' }
    ],
    categoryId: [
      { required: true, message: '请选择菜系', trigger: 'change' }
    ]
  }
})

/* wangEditor5 初始化开始 */
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()
const mode = 'default'
const editorConfig = { MENU_CONF: {} }
// 核心修改：配置wangEditor图片上传大小限制（和后端一致100MB）
editorConfig.MENU_CONF['uploadImage'] = {
  server: baseUrl + '/files/wang/upload',
  fieldName: 'file',
  maxFileSize: 100 * 1024 * 1024, // 100MB，单位字节
  onError: (err) => {
    ElMessage.error(`图片上传失败：${err.msg || '文件大小超出限制'}`)
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = editor
}
/* wangEditor5 初始化结束 */

const view = (content) => {
  data.content = content
  data.viewVisible = true
}

//上传前校验文件大小（100MB）
const beforeUpload = (file) => {
  const maxSize = 100 * 1024 * 1024; // 100MB
  const isOverSize = file.size > maxSize;
  if (isOverSize) {
    ElMessage.error(`上传文件大小不能超过100MB！当前文件大小：${(file.size / 1024 / 1024).toFixed(2)}MB`);
    return false; // 阻止上传
  }
  return true;
}

//处理文件超出限制/数量的情况
const handleExceed = (files) => {
  ElMessage.error(`只能上传1张图片，且文件大小不能超过100MB！`);
}

//移除上传文件时清空绑定值
const handleRemove = () => {
  data.form.img = '';
  fileList.value = [];
}

// 处理文件上传成功的钩子
const handleImgSuccess = (res) => {
  if (res.code === '200') {
    data.form.img = res.data;
    fileList.value = [{ url: res.data }]; // 回显上传的图片
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(`图片上传失败：${res.msg}`);
  }
}



// 监听编辑弹窗关闭，清空文件列表
watch(() => data.formVisible, (val) => {
  if (!val) {
    fileList.value = [];
    data.form = {};
  }
})

request.get('/category/selectAll').then(res => {
  data.categoryList = res.data
})

// 分页查询
const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.categoryId
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.tableData.forEach(v =>{
      v.recommendActive = v.recommend === '是'
    })
    data.total = res.data?.total
  })
}
load()

// 新增
const handleAdd = () => {
  data.form = { status: '上架', price: 0, recommend: '否' }
  fileList.value = []; // 清空文件列表
  data.formVisible = true
}

// 编辑：回显已上传的图片
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  // 回显图片到上传组件
  if (row.img) {
    fileList.value = [{ url: row.img }];
  } else {
    fileList.value = [];
  }
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/goods/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const updateRecommed = (row) => {
  data.form.id = row.id
  data.form.recommend = row.recommendActive ? '是' : '否'
  update()
}
// 编辑保存
const update = () => {
  request.put('/goods/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/goods/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.name = null
  data.categoryId = null
  load()
}
</script>