<template>
  <div class="page-container">
    <div class="profile-card">
      <div class="card-header">
        <h2>👤 个人信息</h2>
        <p class="subtitle">完善你的个人资料</p>
      </div>

      <el-form
          ref="formRef"
          :model="data.user"
          :rules="data.rules"
          label-width="80px"
          class="profile-form"
      >
        <!-- 头像上传 -->
        <el-form-item prop="avatar" label="头像" class="avatar-item">
          <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <div class="avatar-wrapper">
              <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="40"><Plus /></el-icon>
                <span>上传头像</span>
              </div>
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
              </div>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 账号（禁用状态，视觉优化） -->
        <el-form-item prop="username" label="账号">
          <el-input
              :disabled="data.user.id !== undefined"
              v-model="data.user.username"
              placeholder="请输入账号"
              class="custom-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 姓名 -->
        <el-form-item prop="name" label="姓名">
          <el-input
              v-model="data.user.name"
              placeholder="请输入姓名"
              class="custom-input"
          >
            <template #prefix>
              <el-icon><Edit /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 保存按钮 -->
        <div class="form-footer">
          <el-button type="primary" size="large" @click="update" :loading="loading">
            保存修改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Plus, Camera, User, Edit } from "@element-plus/icons-vue";

const emit = defineEmits(['updateUser']);

const baseUrl = import.meta.env.VITE_BASE_URL;
const formRef = ref();
const loading = ref(false);

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
    ]
  }
});

const loadUser = () => {
  request.get('/admin/selectById/' + data.user.id).then(res => {
    data.user = res.data;
    localStorage.setItem('system-user', JSON.stringify(res.data));
    emit('updateUser');
  });
};
loadUser();

const handleFileUpload = (res) => {
  data.user.avatar = res.data;
};

const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.put('/admin/update', data.user).then(res => {
        loading.value = false;
        if (res.code === '200') {
          ElMessage.success('个人信息更新成功！');
          loadUser();
        } else {
          ElMessage.error(res.msg || '更新失败');
        }
      }).catch(() => {
        loading.value = false;
        ElMessage.error('网络异常，请稍后重试');
      });
    }
  });
};
</script>

<style scoped>
.page-container {
  min-height: 100%;
  padding: 20px;
  background: #f5f7fa;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.profile-card {
  width: 520px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05), 0 6px 12px rgba(0, 0, 0, 0.03);
  padding: 32px 36px;
  transition: transform 0.2s;
}

.profile-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.profile-form {
  margin-top: 8px;
}

.avatar-item {
  margin-bottom: 28px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;
}

.avatar-wrapper:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #eef2ff 0%, #e0e7ff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 14px;
  gap: 6px;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.25s;
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 14px;
  padding: 4px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  box-shadow: none;
  transition: all 0.2s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  background: #ffffff;
  border-color: #cbd5e1;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

:deep(.custom-input .el-input__inner) {
  font-size: 15px;
}

:deep(.custom-input .el-input__prefix) {
  color: #94a3b8;
  margin-right: 8px;
}

.form-footer {
  margin-top: 36px;
  text-align: center;
}

.el-button {
  min-width: 160px;
  border-radius: 40px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.25);
  transition: all 0.2s;
}

.el-button:hover {
  background: linear-gradient(135deg, #4f52e0 0%, #7c3aed 100%);
  box-shadow: 0 12px 20px rgba(99, 102, 241, 0.35);
  transform: scale(1.02);
}

.el-button:active {
  transform: scale(0.98);
}
</style>