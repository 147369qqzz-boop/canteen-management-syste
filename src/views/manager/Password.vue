<template>
  <div class="page-container">
    <div class="password-card">
      <div class="card-header">
        <h2>🔐 修改密码</h2>
        <p class="subtitle">为保障账户安全，请定期更换密码</p>
      </div>

      <el-form
          ref="formRef"
          :model="data.user"
          :rules="data.rules"
          label-width="100px"
          class="password-form"
      >
        <el-form-item prop="password" label="原密码">
          <el-input
              v-model="data.user.password"
              placeholder="请输入原密码"
              show-password
              class="custom-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="newPassword" label="新密码">
          <el-input
              v-model="data.user.newPassword"
              placeholder="请输入新密码"
              show-password
              class="custom-input"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
              v-model="data.user.confirmPassword"
              placeholder="请再次输入新密码"
              show-password
              class="custom-input"
          >
            <template #prefix>
              <el-icon><CircleCheck /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码强度提示（可选） -->
        <div class="strength-tip" v-if="data.user.newPassword">
          <span :class="['strength-bar', strengthClass]"></span>
          <span class="strength-text">{{ strengthText }}</span>
        </div>

        <div class="form-footer">
          <el-button type="primary" size="large" @click="updatePassword" :loading="loading">
            确认修改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Lock, Key, CircleCheck } from "@element-plus/icons-vue";
import router from "@/router";

const formRef = ref();
const loading = ref(false);

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    password: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== data.user.newPassword) {
            callback(new Error('两次输入的密码不一致'));
          } else {
            callback();
          }
        },
        trigger: 'blur'
      }
    ]
  }
});

// 简单的密码强度计算
const strengthLevel = computed(() => {
  const pwd = data.user.newPassword || '';
  if (pwd.length === 0) return 0;
  let score = 0;
  if (pwd.length >= 8) score++;
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
  if (/\d/.test(pwd)) score++;
  if (/[^a-zA-Z0-9]/.test(pwd)) score++;
  return Math.min(score, 3);
});

const strengthClass = computed(() => {
  return ['weak', 'medium', 'strong', 'very-strong'][strengthLevel.value] || '';
});

const strengthText = computed(() => {
  return ['', '强度：弱', '强度：中', '强度：强'][strengthLevel.value] || '';
});

const updatePassword = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.put('/updatePassword', data.user).then(res => {
        loading.value = false;
        if (res.code === '200') {
          ElMessage.success('密码修改成功，请重新登录');
          logout();
        } else {
          ElMessage.error(res.msg || '修改失败');
        }
      }).catch(() => {
        loading.value = false;
        ElMessage.error('网络异常，请稍后重试');
      });
    }
  });
};

const logout = () => {
  router.push('/login');
  localStorage.removeItem('system-user');
  localStorage.removeItem('token');
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

.password-card {
  width: 520px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05), 0 6px 12px rgba(0, 0, 0, 0.03);
  padding: 32px 36px;
  transition: transform 0.2s;
}

.password-card:hover {
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

.password-form {
  margin-top: 8px;
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
  border-color: #f59e0b;
  box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.1);
}

:deep(.custom-input .el-input__inner) {
  font-size: 15px;
}

:deep(.custom-input .el-input__prefix) {
  color: #94a3b8;
  margin-right: 8px;
}

.strength-tip {
  display: flex;
  align-items: center;
  margin: 16px 0 8px 100px;
  gap: 12px;
}

.strength-bar {
  width: 80px;
  height: 6px;
  background: #e2e8f0;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.strength-bar::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 0%;
  background: #cbd5e1;
  border-radius: 10px;
  transition: width 0.3s, background 0.3s;
}

.strength-bar.weak::after {
  width: 25%;
  background: #ef4444;
}

.strength-bar.medium::after {
  width: 50%;
  background: #f59e0b;
}

.strength-bar.strong::after {
  width: 75%;
  background: #3b82f6;
}

.strength-bar.very-strong::after {
  width: 100%;
  background: #10b981;
}

.strength-text {
  font-size: 13px;
  color: #64748b;
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
  background: linear-gradient(135deg, #f59e0b 0%, #ea580c 100%);
  border: none;
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.25);
  transition: all 0.2s;
}

.el-button:hover {
  background: linear-gradient(135deg, #d97706 0%, #c2410c 100%);
  box-shadow: 0 12px 20px rgba(245, 158, 11, 0.35);
  transform: scale(1.02);
}

.el-button:active {
  transform: scale(0.98);
}
</style>