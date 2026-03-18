<template>
  <div class="login-container">
    <ParticleBackground />
    <WaveBackground />
    <div class="login-box maritime-glass maritime-float">
      <div class="login-header">
        <div class="icon-wrapper maritime-glow">
          <el-icon :size="60" color="#ffd700"><Ship /></el-icon>
        </div>
        <h1 class="maritime-gradient-text">E-ship 船舶租赁平台</h1>
        <p>连接船东与租家，让租船更简单</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <el-input
            v-model="loginForm.verificationCode"
            placeholder="请输入验证码"
            prefix-icon="Message"
            clearable
          >
            <template #append>
              <el-button
                @click="handleSendCode"
                :disabled="countdown > 0"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="login-footer">
            <el-link type="primary" @click="$router.push('/register')">
              还没有账号？立即注册
            </el-link>
            <el-link type="info" @click="showResetDialog = true">忘记密码？</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="showResetDialog"
      title="重置密码"
      width="600px"
      class="maritime-dialog"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="resetForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="resetForm.email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="验证码" prop="verificationCode">
          <el-input
            v-model="resetForm.verificationCode"
            placeholder="请输入验证码"
            clearable
          >
            <template #append>
              <el-button
                @click="handleSendResetCode"
                :disabled="resetCountdown > 0"
                :loading="sendingResetCode"
              >
                {{ resetCountdown > 0 ? `${resetCountdown}秒后重试` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少12位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="resetting">
          重置密码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, sendCode, getEmailByUsername, resetPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import ParticleBackground from '@/components/ParticleBackground.vue'
import WaveBackground from '@/components/WaveBackground.vue'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const resetFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const showResetDialog = ref(false)
const sendingResetCode = ref(false)
const resetCountdown = ref(0)
const resetting = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  verificationCode: ''
})

const resetForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在2-30个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 12, message: '密码不少于12位', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

const resetRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在2-30个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 12, message: '密码不少于12位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  if (!loginForm.username) {
    ElMessage.warning('请先输入用户名')
    return
  }

  sendingCode.value = true
  try {
    // 先获取用户邮箱
    const emailRes = await getEmailByUsername(loginForm.username)
    const email = emailRes.data

    // 发送验证码
    await sendCode({ email, type: 'LOGIN' })
    ElMessage.success('验证码已发送到您的邮箱')

    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// 登录
const handleLogin = async () => {
  await loginFormRef.value.validate()

  loading.value = true
  try {
    const res = await login(loginForm)

    // 保存token和用户信息
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)

    ElMessage.success('登录成功')

    // 根据角色跳转
    const role = res.data.userInfo.role
    if (role === 'SHIP_OWNER') {
      router.push('/owner/ships')
    } else if (role === 'RENTER') {
      router.push('/renter/search')
    } else if (role === 'ADMIN') {
      router.push('/admin')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 发送重置密码验证码
const handleSendResetCode = async () => {
  if (!resetForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  sendingResetCode.value = true
  try {
    await sendCode({ email: resetForm.email, type: 'RESET_PASSWORD' })
    ElMessage.success('验证码已发送到您的邮箱')

    // 开始倒计时
    resetCountdown.value = 60
    const timer = setInterval(() => {
      resetCountdown.value--
      if (resetCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sendingResetCode.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请填写完整的信息')
    return
  }

  resetting.value = true
  try {
    await resetPassword({
      username: resetForm.username,
      email: resetForm.email,
      verificationCode: resetForm.verificationCode,
      newPassword: resetForm.newPassword
    })

    ElMessage.success('密码重置成功，请使用新密码登录')
    showResetDialog.value = false

    // 清空表单
    Object.assign(resetForm, {
      username: '',
      email: '',
      verificationCode: '',
      newPassword: '',
      confirmPassword: ''
    })
    resetFormRef.value?.resetFields()
  } catch (error) {
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--maritime-gradient-ocean);
  overflow: hidden;
}

.login-box {
  position: relative;
  z-index: 10;
  width: 480px;
  padding: 50px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--maritime-radius-xl);
  box-shadow: var(--maritime-shadow-lg);
  border: 2px solid rgba(30, 144, 255, 0.2);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.icon-wrapper {
  display: inline-block;
  padding: 20px;
  background: var(--maritime-gradient-primary);
  border-radius: 50%;
  margin-bottom: 20px;
}

.login-header h1 {
  margin: 20px 0 10px;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
}

.login-header p {
  color: var(--maritime-gray);
  font-size: 15px;
  margin-top: 10px;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-input__wrapper) {
  border: 2px solid rgba(30, 144, 255, 0.2);
  border-radius: var(--maritime-radius-md);
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: none;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--maritime-blue);
  box-shadow: var(--maritime-shadow-glow);
}

.login-form :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: var(--maritime-blue);
  box-shadow: var(--maritime-shadow-glow);
}

.login-form :deep(.el-button--primary) {
  background: var(--maritime-gradient-primary);
  border: none;
  border-radius: var(--maritime-radius-md);
  padding: 14px 24px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
}

.login-form :deep(.el-button--primary:hover) {
  box-shadow: var(--maritime-shadow-glow);
  transform: translateY(-2px);
}

.login-form :deep(.el-input-group__append .el-button) {
  background: var(--maritime-gradient-primary);
  color: white;
  border: none;
  font-weight: 600;
}

.login-form :deep(.el-input-group__append .el-button:hover) {
  opacity: 0.9;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 10px;
}

.login-footer :deep(.el-link) {
  font-size: 14px;
  font-weight: 500;
}

.login-footer :deep(.el-link.el-link--primary) {
  color: var(--maritime-blue);
}

.login-footer :deep(.el-link.el-link--primary:hover) {
  color: var(--maritime-blue-light);
}

/* 对话框整体样式优化 */
:deep(.maritime-dialog) {
  z-index: 3000;
}

:deep(.maritime-dialog .el-dialog) {
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.8),
    0 0 80px rgba(60, 235, 220, 0.3),
    inset 0 0 100px rgba(60, 235, 220, 0.05) !important;
  border: 2px solid rgba(79, 168, 255, 0.4) !important;
  border-radius: 20px !important;
  overflow: hidden;
  position: relative;
}

/* 对话框外发光效果 */
:deep(.maritime-dialog .el-dialog::before) {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(45deg,
    transparent 0%,
    rgba(60, 235, 220, 0.3) 25%,
    rgba(79, 168, 255, 0.3) 50%,
    rgba(255, 214, 92, 0.3) 75%,
    transparent 100%);
  border-radius: 20px;
  opacity: 0.5;
  z-index: -1;
  animation: dialog-border-glow 4s linear infinite;
  background-size: 400% 400%;
  pointer-events: none;
}

@keyframes dialog-border-glow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

:deep(.maritime-dialog .el-dialog__header) {
  position: relative;
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.95), rgba(79, 168, 255, 0.95)) !important;
  color: white;
  padding: 28px 32px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

/* 标题区域装饰图案 */
:deep(.maritime-dialog .el-dialog__header::after) {
  content: '';
  position: absolute;
  right: -50px;
  top: 50%;
  transform: translateY(-50%);
  width: 150px;
  height: 150px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 150 150"><circle cx="75" cy="75" r="60" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="8,6"/><circle cx="75" cy="75" r="45" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="2"/><circle cx="75" cy="75" r="30" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="1.5" stroke-dasharray="4,4"/><path d="M50,75 L60,70 L70,73 L80,67 L90,70 L100,65" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="2"/></svg>') no-repeat center;
  background-size: contain;
  pointer-events: none;
  opacity: 0.3;
  animation: header-decoration-rotate 20s linear infinite;
}

@keyframes header-decoration-rotate {
  from { transform: translateY(-50%) rotate(0deg); }
  to { transform: translateY(-50%) rotate(360deg); }
}

/* 对话框标题装饰 */
:deep(.maritime-dialog .el-dialog__header::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: header-shine 3s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

@keyframes header-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

:deep(.maritime-dialog .el-dialog__title) {
  color: white;
  font-weight: 800;
  font-size: 24px;
  letter-spacing: 3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(255, 255, 255, 0.3),
    0 0 40px rgba(60, 235, 220, 0.2);
  position: relative;
  z-index: 2;
  text-transform: uppercase;
}

:deep(.maritime-dialog .el-dialog__close) {
  color: white;
  font-size: 22px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.maritime-dialog .el-dialog__close:hover) {
  color: var(--yellow-400);
  transform: rotate(90deg) scale(1.1);
  filter: drop-shadow(0 0 15px rgba(255, 214, 92, 0.8));
  background: rgba(255, 214, 92, 0.2);
  box-shadow: 0 0 20px rgba(255, 214, 92, 0.5);
}

:deep(.maritime-dialog .el-dialog__body) {
  position: relative;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  color: #e9f6ff;
  overflow: hidden;
  padding: 32px 24px;
}

/* 对话框背景装饰 */
:deep(.maritime-dialog .el-dialog__body::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 30%, rgba(60, 235, 220, 0.1), transparent 50%),
    radial-gradient(circle at 70% 70%, rgba(79, 168, 255, 0.1), transparent 50%);
  pointer-events: none;
  animation: dialog-glow 4s ease-in-out infinite;
}

@keyframes dialog-glow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

:deep(.maritime-dialog .el-dialog__body::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
}

:deep(.maritime-dialog .el-dialog__footer) {
  position: relative;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.98) 0%, rgba(26, 31, 58, 0.98) 100%) !important;
  border-top: 1px solid rgba(79, 168, 255, 0.2);
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 对话框底部装饰 */
:deep(.maritime-dialog .el-dialog__footer::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8), transparent);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.5);
  pointer-events: none;
}

:deep(.maritime-dialog .el-form-item__label) {
  color: #ffd65c;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.5px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.6);
  position: relative;
  padding-left: 12px;
  line-height: 32px;
}

/* 表单标签装饰 */
:deep(.maritime-dialog .el-form-item__label::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: linear-gradient(180deg, #ffd65c, #ffb703);
  border-radius: 2px;
  box-shadow: 0 0 8px rgba(255, 214, 92, 0.6);
}

:deep(.maritime-dialog .el-form-item) {
  margin-bottom: 28px;
  position: relative;
}

:deep(.maritime-dialog .el-form-item__content) {
  position: relative;
  line-height: normal;
  display: block;
}

/* 确保错误提示不会与输入框重叠 */
:deep(.maritime-dialog .el-form-item.is-error .el-input__wrapper) {
  border-color: rgba(255, 90, 122, 0.6);
  box-shadow: 0 0 15px rgba(255, 90, 122, 0.3);
}

:deep(.maritime-dialog .el-input__wrapper) {
  background: rgba(15, 43, 75, 0.6);
  border: 1px solid rgba(79, 168, 255, 0.3);
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.1);
  transition: all 0.3s ease;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  min-height: 36px;
}

/* 输入框装饰线 */
:deep(.maritime-dialog .el-input__wrapper::before) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(60, 235, 220, 0.8), rgba(255, 214, 92, 0.8));
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(60, 235, 220, 0.6);
}

:deep(.maritime-dialog .el-input__wrapper:hover) {
  border-color: rgba(60, 235, 220, 0.5);
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.2);
  background: rgba(15, 43, 75, 0.7);
}

:deep(.maritime-dialog .el-input__wrapper.is-focus) {
  border-color: rgba(60, 235, 220, 0.7);
  box-shadow: 0 0 20px rgba(60, 235, 220, 0.3), inset 0 0 20px rgba(60, 235, 220, 0.1);
  background: rgba(15, 43, 75, 0.8);
}

:deep(.maritime-dialog .el-input__wrapper.is-focus::before) {
  width: 100%;
}

:deep(.maritime-dialog .el-input__inner) {
  color: #e9f6ff;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.5;
}

:deep(.maritime-dialog .el-input__inner::placeholder) {
  color: rgba(233, 246, 255, 0.35);
  font-weight: 400;
}

:deep(.maritime-dialog .el-input-group__append) {
  background: rgba(15, 43, 75, 0.8);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-left: none;
}

:deep(.maritime-dialog .el-input-group__append .el-button) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.8), rgba(79, 168, 255, 0.8));
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
}

:deep(.maritime-dialog .el-input-group__append .el-button:hover) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 1), rgba(79, 168, 255, 1));
  box-shadow: 0 0 15px rgba(60, 235, 220, 0.5);
}

:deep(.maritime-dialog .el-input-group__append .el-button:disabled) {
  background: rgba(79, 168, 255, 0.3);
  color: rgba(255, 255, 255, 0.5);
}

/* 按钮样式 */
:deep(.maritime-dialog .el-button) {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 12px 28px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.maritime-dialog .el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.maritime-dialog .el-button:hover::before) {
  width: 300px;
  height: 300px;
}

:deep(.maritime-dialog .el-button--default) {
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  color: #4fa8ff;
}

:deep(.maritime-dialog .el-button--default:hover) {
  background: rgba(79, 168, 255, 0.2);
  border-color: rgba(79, 168, 255, 0.5);
  box-shadow: 0 0 20px rgba(79, 168, 255, 0.3);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--primary) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.9), rgba(79, 168, 255, 0.9));
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(60, 235, 220, 0.3);
}

:deep(.maritime-dialog .el-button--primary:hover) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 1), rgba(79, 168, 255, 1));
  box-shadow: 0 6px 25px rgba(60, 235, 220, 0.5);
  transform: translateY(-2px);
}

:deep(.maritime-dialog .el-button--primary.is-loading) {
  background: linear-gradient(135deg, rgba(60, 235, 220, 0.7), rgba(79, 168, 255, 0.7));
}

</style>
