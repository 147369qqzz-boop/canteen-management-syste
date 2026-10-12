<template>
  <div class="front-container" style="width: 80%">
    <div style="margin-bottom: 10px;">
      <el-input clearable @clear="load" v-model="data.orderNo" style="width: 400px; height: 40px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
      <el-input clearable @clear="load" v-model="data.goodsName" style="width: 400px; height: 40px; margin-right: 10px" placeholder="请输入菜品名称查询"></el-input>
      <el-button style="height: 40px" type="primary" @click="load">查 询</el-button>
      <el-button style="height: 40px" @click="reset">重 置</el-button>
    </div>

    <div class="card">
      <el-table :data="data.tableData" stripe v-loading="loading" default-expand-all>
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px">
              <el-table :data="props.row.orderDetailList" border>
                <el-table-column label="菜品图片" prop="goodsImg" width="100">
                  <template #default="scope">
                    <img :src="scope.row.goodsImg" alt="" style="width: 50px; height: 50px">
                  </template>
                </el-table-column>
                <el-table-column label="菜品名称" prop="goodsName" show-overflow-tooltip></el-table-column>
                <el-table-column label="菜品单价" prop="goodsPrice" width="100"></el-table-column>
                <el-table-column label="数量" prop="num" width="100">
                  <template #default="scope">X {{ scope.row.num }}</template>
                </el-table-column>
                <el-table-column label="小计" width="150">
                  <template #default="scope">
                    <b style="color: red">{{ (scope.row.goodsPrice * scope.row.num).toFixed(2) }} 元</b>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button @click="handleAddComment(scope.row)" type="success" v-if="props.row.status === '已完成'">评价</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="240">
          <template #default="scope"><b>{{ scope.row.orderNo }}</b></template>
        </el-table-column>
        <el-table-column prop="total" label="总价格">
          <template #default="scope"><b style="color: red">{{ scope.row.total }}元</b></template>
        </el-table-column>
        <el-table-column prop="deliverType" label="配送类型"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
            <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间" width="160"></el-table-column>
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="deliver" label="配送信息" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="订单操作" align="center" width="120">
          <template #default="scope">
            <el-button @click="cancel(scope.row)" type="danger" v-if="scope.row.status === '待接单'">取消</el-button>
            <el-button @click="done(scope.row)" type="primary" v-if="scope.row.status === '已出货' || scope.row.status === '已配送'">确认收货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <el-dialog title="评价信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="评分" prop="score">
          <el-rate show-score allow-half v-model="data.form.score"></el-rate>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="3" v-model="data.form.content" autocomplete="off" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
import { getLoginUser } from '@/utils/auth'

const formRef = ref()
const loading = ref(false)
const data = reactive({
  user: {},
  pageNum: 1,
  pageSize: 3,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  orderNo: null,
  goodsName: null,
  rules: {
    score: [{ required: true, message: '请输入评分', trigger: 'change' }],
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  }
})

const load = async () => {
  data.user = getLoginUser()
  const userId = data.user?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  loading.value = true
  try {
    const res = await request.get('/orders/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        orderNo: data.orderNo,
        goodsName: data.goodsName,
        userId: userId
      }
    })
    const pageData = res.data || {}
    data.tableData = pageData.list || []
    data.total = pageData.total || 0
  } catch (err) {
    console.error(err)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}
load()

// 取消订单
const cancel = (row) => {
  ElMessageBox.confirm('您确认取消订单吗?', '二次确认', { type: 'warning' }).then(() => {
    data.form = row
    data.form.status = '已取消'
    updateOrder()
  }).catch(() => {})
}

// 确认收货
const done = (row) => {
  ElMessageBox.confirm('您确认订单货物已经收到了吗?', '二次确认', { type: 'warning' }).then(() => {
    data.form = row
    data.form.status = '已完成'
    updateOrder()
  }).catch(() => {})
}

const updateOrder = () => {
  request.put('/orders/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  })
}

// 打开评价
const handleAddComment = (row) => {
  request.get('/comment/selectAll', {
    params: {
      orderId: row.id,
      userId: data.user.id
    }
  }).then(res => {
    data.form = res.data?.length > 0 ? res.data[0] : {
      orderId: row.id,
      userId: data.user.id,
      score: 5
    }
    data.formVisible = true
  })
}

// 新增评价
const addComment = () => {
  request.post('/comment/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('评价成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  })
}

// 修改评价
const updateComment = () => {
  request.put('/comment/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  })
}

// 保存评价
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? updateComment() : addComment()
    }
  })
}

// 重置搜索
const reset = () => {
  data.orderNo = null
  data.goodsName = null
  data.pageNum = 1
  load()
}
</script>

<style scoped>
.el-tag--warning {
  color: orange;
  background-color: #fff2de;
}
</style>