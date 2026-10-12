<template>
  <div class="login-container">
    <!-- 登录卡片 -->
    <div class="login-box">
      <div class="logo-area">
        <div class="logo-icon">🍱</div>
        <h2 class="welcome-text">校园食堂 · 美食每刻</h2>
        <p class="slogan">登录探索今日美味</p>
      </div>

      <el-form :model="data.form" ref="formRef" :rules="data.rules" class="login-form">
        <el-form-item prop="username">
          <el-input
              :prefix-icon="User"
              size="large"
              v-model="data.form.username"
              placeholder="请输入账号"
              class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              :prefix-icon="Lock"
              size="large"
              v-model="data.form.password"
              placeholder="请输入密码"
              show-password
              class="custom-input"
          />
        </el-form-item>
        <!-- 角色选择：新增 商家 选项 -->
        <el-form-item prop="role">
          <el-select
              size="large"
              style="width: 100%"
              v-model="data.form.role"
              placeholder="选择身份"
              class="custom-select"
          >
            <el-option value="普通用户" label="🍽️ 普通用户"></el-option>
            <el-option value="管理员" label="👩‍🍳 管理员"></el-option>
            <el-option value="商家" label="🏪 商家"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
              size="large"
              type="primary"
              class="login-btn"
              @click="login"
              :loading="data.loading"
          >
            <span v-if="!data.loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        还没有账号？ <a href="/register">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";
import { setLoginUser, setToken } from "@/utils/auth";

const data = reactive({
  form: { role: '管理员' }, // 默认选中管理员
  loading: false,
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  }
});

const formRef = ref();

const login = () => {
  // 1. 防止重复提交
  if (data.loading) return;
  data.loading = true;

  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/login', data.form)
          .then(res => {
            if (res.code === '200') {
              ElMessage.success("登录成功！");
              setToken(res.data.token);
              setLoginUser(res.data.user);
              if (res.data.user.role === '管理员') {
                router.push('/manager/home');
              } else if (res.data.user.role === '商家') {
                router.push('/business/dashboard');
              } else {
                router.push('/front/home');
              }
            } else {
              ElMessage.error(res.msg || '登录失败');
            }
          })
          .catch(() => {
            ElMessage.error('网络异常');
          })
          .finally(() => {
            data.loading = false;   // 无论成功/失败，最终释放锁
          });
    } else {
      data.loading = false;       // 验证失败也要释放锁
    }
  });
};
</script>

<style scoped>
/* 登录容器 + 背景图片 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 核心：背景图片 */
  background: url("@/assets/imgs/bg.jpg") no-repeat center;
  background-size: cover;
  position: relative;
}
/* 遮罩层（让背景不刺眼，文字更清晰） */
.login-container::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.3);
  z-index: 1;
}

/* 登录卡片（不变，优化层级） */
.login-box {
  width: 420px;
  padding: 45px 40px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  box-shadow: 0 30px 50px rgba(0,0,0,0.3);
  z-index: 2;
}

.logo-area { text-align: center; margin-bottom: 36px; }
.logo-icon { font-size: 52px; margin-bottom: 8px; }
.welcome-text { font-size: 26px; font-weight: 700; color: #1e2f3e; margin: 10px 0 6px; }
.slogan { color: #6a7f8f; font-size: 15px; margin: 0; }
.login-form { margin-top: 10px; }

::v-deep .custom-input .el-input__wrapper {
  background: rgba(255,255,255,0.8);
  border-radius: 16px;
  border: 1px solid rgba(200,180,160,0.2);
}
::v-deep .custom-select .el-input__wrapper {
  background: rgba(255,255,255,0.8);
  border-radius: 16px;
}

.login-btn {
  width: 100%; height: 48px; border-radius: 40px;
  background: linear-gradient(135deg, #f38a5e 0%, #e05a3c 100%);
  font-size: 18px; font-weight: 600;
}

.register-link {
  text-align: right; margin-top: 24px; font-size: 15px; color: #5e707e;
}
.register-link a { color: #e0533c; font-weight: 600; }
</style>