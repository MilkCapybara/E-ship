<template>
  <div class="register-container">
    <ParticleBackground />
    <WaveBackground />
    <div class="register-box maritime-glass maritime-float">
      <div class="register-header">
        <div class="icon-wrapper maritime-glow">
          <el-icon :size="60" color="#ffd700"><Ship /></el-icon>
        </div>
        <h1 class="maritime-gradient-text">注册 E-ship 账号</h1>
        <p>加入船舶租赁平台，开启便捷租船之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（2-30个字符）"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（不少于12位）"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="role">
          <MaritimeSelect
            v-model="registerForm.role"
            :options="roleOptions"
            placeholder="请选择角色"
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <el-input
            v-model="registerForm.verificationCode"
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

        <el-form-item prop="companyName">
          <el-input
            v-model="registerForm.companyName"
            placeholder="公司名称（可选）"
            prefix-icon="OfficeBuilding"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleRegister"
            :loading="loading"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-footer">
            <el-link type="primary" @click="$router.push('/login')">
              已有账号？立即登录
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendCode } from '@/api/auth'
import ParticleBackground from '@/components/ParticleBackground.vue'
import WaveBackground from '@/components/WaveBackground.vue'
import MaritimeSelect from '@/components/MaritimeSelect.vue'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: '',
  companyName: '',
  code: ''
})

const roleOptions = [
  { label: '船东（出租方）', value: 'SHIP_OWNER' },
  { label: '租家（承租方）', value: 'RENTER' }
]

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 12) {
    callback(new Error('密码不少于12位'))
  } else if (!/[A-Z]/.test(value)) {
    callback(new Error('密码必须包含大写字母'))
  } else if (!/[a-z]/.test(value)) {
    callback(new Error('密码必须包含小写字母'))
  } else if (!/[0-9]/.test(value)) {
    callback(new Error('密码必须包含数字'))
  } else if (!/[!@#$%^&*(),.?":{}|<>]/.test(value)) {
    callback(new Error('密码必须包含特殊字符'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在2-30个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  sendingCode.value = true
  try {
    await sendCode({ email: registerForm.email, type: 'REGISTER' })
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

const handleRegister = async () => {
  await registerFormRef.value.validate()

  loading.value = true
  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--maritime-gradient-ocean);
  overflow: hidden;
}

.register-box {
  position: relative;
  z-index: 10;
  width: 520px;
  padding: 50px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--maritime-radius-xl);
  box-shadow: var(--maritime-shadow-lg);
  border: 2px solid rgba(30, 144, 255, 0.2);
  backdrop-filter: blur(10px);
}

.register-header {
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

.register-header h1 {
  margin: 20px 0 10px;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
}

.register-header p {
  color: var(--maritime-gray);
  font-size: 15px;
  margin-top: 10px;
}

.register-form {
  margin-top: 30px;
}

.register-form :deep(.el-input__wrapper) {
  border: 2px solid rgba(30, 144, 255, 0.2);
  border-radius: var(--maritime-radius-md);
  padding: 12px 16px;
  transition: all 0.3s ease;
  box-shadow: none;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: var(--maritime-blue);
  box-shadow: var(--maritime-shadow-glow);
}

.register-form :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: var(--maritime-blue);
  box-shadow: var(--maritime-shadow-glow);
}

.register-form :deep(.el-button--primary) {
  background: var(--maritime-gradient-primary);
  border: none;
  border-radius: var(--maritime-radius-md);
  padding: 14px 24px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
}

.register-form :deep(.el-button--primary:hover) {
  box-shadow: var(--maritime-shadow-glow);
  transform: translateY(-2px);
}

.register-form :deep(.el-input-group__append .el-button) {
  background: var(--maritime-gradient-primary);
  color: white;
  border: none;
  font-weight: 600;
}

.register-form :deep(.el-input-group__append .el-button:hover) {
  opacity: 0.9;
}

.register-footer {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 10px;
}

.register-footer :deep(.el-link) {
  font-size: 14px;
  font-weight: 500;
  color: var(--maritime-blue);
}

.register-footer :deep(.el-link:hover) {
  color: var(--maritime-blue-light);
}
</style>
