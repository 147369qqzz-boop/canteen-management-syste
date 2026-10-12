<template>
  <div class="settings-page">
    <!-- 店铺信息 -->
    <div class="settings-section">
      <div class="section-header">
        <span class="section-title">店铺信息</span>
        <span class="section-desc">完善您的店铺资料，让更多学生认识您</span>
      </div>
      <el-form :model="shopForm" label-width="100px" v-loading="loadingShop" class="settings-form">
        <el-form-item label="店铺头像">
          <el-upload :action="uploadUrl" :on-success="onShopAvatar" :show-file-list="false" accept="image/*">
            <el-avatar :src="shopForm.avatar" :size="88" shape="square" style="border-radius:14px" />
            <span class="upload-hint">点击更换头像</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="店铺名称" required>
          <el-input v-model="shopForm.businessName" placeholder="请输入店铺/窗口名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="shopForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="shopForm.address" type="textarea" :rows="2" placeholder="请输入店铺具体位置" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveShop" :loading="savingShop">保存店铺信息</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 个人账号 -->
    <div class="settings-section">
      <div class="section-header">
        <span class="section-title">个人账号</span>
        <span class="section-desc">管理您的个人账户信息</span>
      </div>
      <el-form :model="userForm" label-width="100px" v-loading="loadingUser" class="settings-form">
        <el-form-item label="个人头像">
          <el-upload :action="uploadUrl" :on-success="onUserAvatar" :show-file-list="false" accept="image/*">
            <el-avatar :src="userForm.avatar" :size="88" />
            <span class="upload-hint">点击更换头像</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.name" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="userForm.sex">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="userForm.age" :min="0" :max="150" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveUser" :loading="savingUser">保存账号信息</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { getLoginUser, setLoginUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const loadingShop = ref(false)
const loadingUser = ref(false)
const savingShop = ref(false)
const savingUser = ref(false)

const shopForm = reactive({
  id: undefined,
  userId: undefined,
  avatar: '',
  businessName: '',
  phone: '',
  address: ''
})

const userForm = reactive({
  id: undefined,
  avatar: '',
  name: '',
  sex: '男',
  age: 0,
  email: ''
})

const loadShop = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loadingShop.value = true
  try {
    const res = await request.get('/business/info', { params: { userId: user.id } })
    if (res.data) Object.assign(shopForm, res.data)
    shopForm.userId = user.id
  } catch {
    console.error('加载店铺信息失败')
  } finally {
    loadingShop.value = false
  }
}

const loadUser = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  loadingUser.value = true
  try {
    const res = await request.get('/user/selectById/' + user.id)
    if (res.data) {
      userForm.id = user.id
      Object.assign(userForm, {
        avatar: res.data.avatar || '',
        name: res.data.name || '',
        sex: res.data.sex || '男',
        age: res.data.age || 0,
        email: res.data.email || ''
      })
    }
  } catch {
    console.error('加载用户信息失败')
  } finally {
    loadingUser.value = false
  }
}

const onShopAvatar = (res) => {
  if (res.code === '200') {
    shopForm.avatar = res.data
    ElMessage.success('店铺头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const onUserAvatar = (res) => {
  if (res.code === '200') {
    userForm.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const saveShop = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  if (!shopForm.businessName?.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  savingShop.value = true
  try {
    const payload = {
      id: shopForm.id,
      userId: user.id,
      avatar: shopForm.avatar,
      businessName: shopForm.businessName,
      phone: shopForm.phone,
      address: shopForm.address
    }
    if (shopForm.id) {
      await request.put('/business/update', payload)
      ElMessage.success('店铺信息已更新')
    } else {
      await request.post('/business/add', payload)
      ElMessage.success('店铺信息已创建')
    }
    await loadShop()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    savingShop.value = false
  }
}

const saveUser = async () => {
  const user = getLoginUser()
  if (!user?.id) return
  savingUser.value = true
  try {
    const payload = {
      id: user.id,
      avatar: userForm.avatar,
      name: userForm.name,
      sex: userForm.sex,
      age: userForm.age,
      email: userForm.email
    }
    await request.put('/user/update', payload)
    setLoginUser({ ...user, ...payload })
    ElMessage.success('账号信息已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    savingUser.value = false
  }
}

onMounted(() => {
  loadShop()
  loadUser()
})
</script>

<style scoped>
.settings-page {
  max-width: 740px;
}
.settings-section {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  padding: 28px 32px;
  margin-bottom: 20px;
}
.section-header {
  margin-bottom: 24px;
}
.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
}
.section-desc {
  font-size: 13px;
  color: #94a3b8;
  margin-left: 8px;
}
.settings-form {
  max-width: 480px;
}
.upload-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 12px;
  cursor: pointer;
}
</style>
