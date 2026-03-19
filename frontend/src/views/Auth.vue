<template>
  <div class="auth-container">
    <!-- 粒子背景 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <!-- 光效背景 -->
    <div class="light-effects">
      <div class="light-beam light-beam-1"></div>
      <div class="light-beam light-beam-2"></div>
      <div class="light-beam light-beam-3"></div>
    </div>

    <!-- 航运装饰元素 -->
    <div class="ship-decorations">
      <div class="ship-icon ship-1">🚢</div>
      <div class="ship-icon ship-2">⚓</div>
      <div class="ship-icon ship-3">🌊</div>
    </div>

    <!-- 主容器 -->
    <div class="auth-wrapper">
      <div class="auth-card" :class="{ 'flip': isRegisterMode }">
        <!-- 登录面 -->
        <div class="card-face card-front">
          <div class="card-header">
            <h1 class="title title-center">
              <span class="title-icon">⚓</span>
              <span>E-SHIP 船易达</span>
            </h1>
            <p class="subtitle">智慧航运 · 科技领航</p>
          </div>

          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="loginForm.code"
                  placeholder="验证码"
                  prefix-icon="Message"
                  size="large"
                  class="custom-input code-input"
                />
                <el-button
                  @click="sendLoginCode"
                  :disabled="loginCodeCountdown > 0"
                  class="code-button"
                  size="large"
                >
                  {{ loginCodeCountdown > 0 ? `${loginCodeCountdown}秒` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-button login-button"
              @click="handleLogin"
              :loading="loginLoading"
            >
              <span class="button-text">登 录</span>
              <span class="button-glow"></span>
            </el-button>

            <div class="form-footer">
              <el-button text class="forgot-button" @click="showForgotDialog = true">
                忘记密码？
              </el-button>
              <el-button text class="switch-button" @click="switchToRegister">
                还没有账号？立即注册 →
              </el-button>
            </div>
          </el-form>
        </div>

        <!-- 注册面 -->
        <div class="card-face card-back">
          <div class="card-header">
            <h1 class="title title-center">
              <span class="title-icon">🚢</span>
              <span>加入 E-SHIP</span>
            </h1>
            <p class="subtitle">开启智慧航运新时代</p>
          </div>

          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名（3-20字符）"
                prefix-icon="User"
                size="large"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱"
                prefix-icon="Message"
                size="large"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（12位+大小写+数字+特殊字符）"
                prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="role">
              <el-select
                v-model="registerForm.role"
                placeholder="选择角色"
                size="large"
                class="custom-input"
              >
                <el-option label="🚢 船东（Ship Owner）" value="SHIP_OWNER" />
                <el-option label="⚓ 租家（Renter）" value="RENTER" />
              </el-select>
            </el-form-item>

            <el-form-item prop="companyName">
              <el-input
                v-model="registerForm.companyName"
                placeholder="公司名称（可选）"
                prefix-icon="OfficeBuilding"
                size="large"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="code" class="full-width">
              <div class="code-input-wrapper">
                <el-input
                  v-model="registerForm.code"
                  placeholder="验证码"
                  prefix-icon="Key"
                  size="large"
                  class="custom-input code-input"
                />
                <el-button
                  @click="sendRegisterCode"
                  :disabled="registerCodeCountdown > 0"
                  class="code-button"
                  size="large"
                >
                  {{ registerCodeCountdown > 0 ? `${registerCodeCountdown}秒` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-button register-button"
              @click="handleRegister"
              :loading="registerLoading"
            >
              <span class="button-text">注 册</span>
              <span class="button-glow"></span>
            </el-button>

            <div class="form-footer">
              <el-button text class="switch-button" @click="switchToLogin">
                ← 已有账号？立即登录
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="showForgotDialog"
      title="重置密码"
      width="500px"
      class="forgot-password-dialog forgot-dialog"
    >
      <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
        <el-form-item prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
            class="forgot-input"
          />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-input-wrapper">
            <el-input
              v-model="forgotForm.code"
              placeholder="验证码"
              prefix-icon="Key"
              size="large"
              class="forgot-input code-input"
            />
            <el-button
              @click="sendForgotCode"
              :disabled="forgotCodeCountdown > 0"
              class="forgot-code-button"
              size="large"
            >
              {{ forgotCodeCountdown > 0 ? `${forgotCodeCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="新密码（12位+大小写+数字+特殊字符）"
            prefix-icon="Lock"
            size="large"
            show-password
            class="forgot-input"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="确认新密码"
            prefix-icon="Lock"
            size="large"
            show-password
            class="forgot-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showForgotDialog = false" class="forgot-cancel-btn">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="forgotLoading" class="forgot-submit-btn">
          重置密码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register, sendCode, getEmailByUsername, resetPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const forgotFormRef = ref(null)

// 模式切换
const isRegisterMode = ref(false)
const showForgotDialog = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  code: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  role: '',
  companyName: ''
})

// 忘记密码表单
const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载状态
const loginLoading = ref(false)
const registerLoading = ref(false)
const forgotLoading = ref(false)

// 验证码倒计时
const loginCodeCountdown = ref(0)
const registerCodeCountdown = ref(0)
const forgotCodeCountdown = ref(0)

// 表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 12, max: 50, message: '密码长度在12-50个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]+$/,
      message: '密码必须包含大小写字母、数字和特殊字符',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 12, max: 50, message: '密码长度在12-50个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]+$/,
      message: '密码必须包含大小写字母、数字和特殊字符',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 切换到注册
const switchToRegister = () => {
  isRegisterMode.value = true
}

// 切换到登录
const switchToLogin = () => {
  isRegisterMode.value = false
}

// 发送登录验证码
const sendLoginCode = async () => {
  if (!loginForm.username) {
    ElMessage.warning('请先输入用户名')
    return
  }

  try {
    // 1. 通过用户名获取邮箱
    ElMessage.info('正在获取邮箱...')
    const emailRes = await getEmailByUsername(loginForm.username)
    const email = emailRes.data

    // 2. 发送验证码到邮箱
    await sendCode({ email: email, type: 'LOGIN' })
    ElMessage.success('验证码已发送到您的邮箱，请查收')

    // 3. 开始倒计时
    loginCodeCountdown.value = 60
    const timer = setInterval(() => {
      loginCodeCountdown.value--
      if (loginCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 发送注册验证码
const sendRegisterCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  try {
    await sendCode({ email: registerForm.email, type: 'REGISTER' })
    ElMessage.success('验证码已发送，请查收邮件')

    // 开始倒计时
    registerCodeCountdown.value = 60
    const timer = setInterval(() => {
      registerCodeCountdown.value--
      if (registerCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    const res = await login(loginForm)

    // 保存token和用户信息
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)

    ElMessage.success('登录成功')

    // 根据角色跳转
    const role = res.data.userInfo.role
    if (role === 'SHIP_OWNER') {
      router.push('/owner')
    } else if (role === 'RENTER') {
      router.push('/renter')
    } else if (role === 'ADMIN') {
      router.push('/admin')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true

    await register(registerForm)

    ElMessage.success('注册成功，请登录')

    // 切换到登录
    switchToLogin()

    // 清空注册表单
    registerFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

// 发送忘记密码验证码
const sendForgotCode = async () => {
  if (!forgotForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  try {
    await sendCode({ email: forgotForm.email, type: 'RESET_PASSWORD' })
    ElMessage.success('验证码已发送，请查收邮件')

    // 开始倒计时
    forgotCodeCountdown.value = 60
    const timer = setInterval(() => {
      forgotCodeCountdown.value--
      if (forgotCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 处理重置密码
const handleResetPassword = async () => {
  try {
    await forgotFormRef.value.validate()
    forgotLoading.value = true

    await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword
    })

    ElMessage.success('密码重置成功，请使用新密码登录')
    showForgotDialog.value = false

    // 清空表单
    forgotFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    forgotLoading.value = false
  }
}

// 粒子系统
const particleCanvas = ref(null)
let particles = []
let animationId = null

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.size = Math.random() * 3 + 1
    this.speedX = Math.random() * 2 - 1
    this.speedY = Math.random() * 2 - 1
    this.color = Math.random() > 0.5 ? 'rgba(0, 150, 255, 0.6)' : 'rgba(255, 200, 0, 0.6)'
  }

  update() {
    this.x += this.speedX
    this.y += this.speedY

    if (this.x > this.canvas.width) this.x = 0
    if (this.x < 0) this.x = this.canvas.width
    if (this.y > this.canvas.height) this.y = 0
    if (this.y < 0) this.y = this.canvas.height
  }

  draw(ctx) {
    ctx.fillStyle = this.color
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()

    // 添加光晕效果
    ctx.shadowBlur = 10
    ctx.shadowColor = this.color
  }
}

const initParticles = () => {
  const canvas = particleCanvas.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const ctx = canvas.getContext('2d')

  // 创建粒子
  particles = []
  for (let i = 0; i < 100; i++) {
    particles.push(new Particle(canvas))
  }

  // 动画循环
  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 绘制连接线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const distance = Math.sqrt(dx * dx + dy * dy)

        if (distance < 150) {
          ctx.strokeStyle = `rgba(0, 150, 255, ${0.2 * (1 - distance / 150)})`
          ctx.lineWidth = 1
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.stroke()
        }
      }
    }

    // 更新和绘制粒子
    particles.forEach(particle => {
      particle.update()
      particle.draw(ctx)
    })

    animationId = requestAnimationFrame(animate)
  }

  animate()
}

// 窗口大小改变
const handleResize = () => {
  if (particleCanvas.value) {
    particleCanvas.value.width = window.innerWidth
    particleCanvas.value.height = window.innerHeight
  }
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.auth-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #0a1929 0%, #1a2332 50%, #0d1b2a 100%);
}

/* 粒子画布 */
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 光效背景 */
.light-effects {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
}

.light-beam {
  position: absolute;
  width: 2px;
  height: 100%;
  background: linear-gradient(180deg, transparent, rgba(0, 150, 255, 0.5), transparent);
  animation: beamMove 8s linear infinite;
}

.light-beam-1 {
  left: 20%;
  animation-delay: 0s;
}

.light-beam-2 {
  left: 50%;
  animation-delay: 2s;
  background: linear-gradient(180deg, transparent, rgba(255, 200, 0, 0.5), transparent);
}

.light-beam-3 {
  left: 80%;
  animation-delay: 4s;
}

@keyframes beamMove {
  0%, 100% {
    transform: translateY(-100%);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
}

/* 航运装饰元素 */
.ship-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
}

.ship-icon {
  position: absolute;
  font-size: 60px;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.ship-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.ship-2 {
  top: 70%;
  right: 15%;
  animation-delay: 2s;
}

.ship-3 {
  bottom: 15%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

/* 主容器 */
.auth-wrapper {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  perspective: 1500px;
}

/* 卡片容器 */
.auth-card {
  position: relative;
  width: 850px;
  max-height: 90vh;
  min-height: 550px;
  transform-style: preserve-3d;
  transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.auth-card.flip {
  transform: rotateY(180deg);
}

/* 卡片面 */
.card-face {
  position: absolute;
  width: 100%;
  max-height: 90vh;
  overflow-y: hidden;
  backface-visibility: hidden;
  background: rgba(15, 30, 50, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 15px 40px 25px 40px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 40px rgba(0, 150, 255, 0.2),
    inset 0 0 60px rgba(0, 150, 255, 0.05);
  border: 1px solid rgba(0, 150, 255, 0.3);
}

/* 注册面特殊布局 */
.card-back {
  display: flex;
  flex-direction: column;
  padding: 20px 35px;
}

.card-back .card-header {
  margin-bottom: 15px;
}

.card-back .auth-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 25px;
  margin-top: 0;
}

.card-back .auth-form .el-form-item {
  margin-bottom: 16px;
}

/* 让某些表单项占满整行 */
.card-back .auth-form .el-form-item.full-width {
  grid-column: 1 / -1;
}

.card-back .submit-button {
  grid-column: 1 / -1;
  margin-top: 10px;
}

.card-back .form-footer {
  grid-column: 1 / -1;
  margin-top: 10px;
}

/* 自定义滚动条样式 */
.card-face::-webkit-scrollbar {
  width: 8px;
}

.card-face::-webkit-scrollbar-track {
  background: rgba(0, 150, 255, 0.1);
  border-radius: 4px;
}

.card-face::-webkit-scrollbar-thumb {
  background: rgba(0, 150, 255, 0.5);
  border-radius: 4px;
}

.card-face::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 150, 255, 0.7);
}

.card-front {
  transform: rotateY(0deg);
}

.card-back {
  transform: rotateY(180deg);
}

/* 卡片头部 */
.card-header {
  text-align: center;
  margin: 0 0 20px 0;
  padding: 0;
}

.title {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #0096ff 0%, #ffc800 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  padding: 0;
  text-shadow: 0 0 30px rgba(0, 150, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  line-height: 1;
}

.title-center {
  flex-direction: column;
  gap: 5px;
  padding-top: 0;
  margin-top: 0;
}

.title-icon {
  font-size: 40px;
  animation: rotate 4s linear infinite;
  margin: 0;
  padding: 0;
  line-height: 1;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px;
  margin: 6px 0 0 0;
  padding: 0;
}

/* 表单样式 */
.auth-form {
  margin-top: 20px;
}

.auth-form .el-form-item {
  margin-bottom: 22px;
}

:deep(.custom-input .el-input__wrapper) {
  background: rgba(0, 150, 255, 0.1);
  border: 1px solid rgba(0, 150, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.1);
  transition: all 0.3s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: rgba(0, 150, 255, 0.6);
  box-shadow: 0 0 30px rgba(0, 150, 255, 0.3);
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #0096ff;
  box-shadow: 0 0 40px rgba(0, 150, 255, 0.5);
}

:deep(.custom-input .el-input__inner) {
  color: #fff;
}

:deep(.custom-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.custom-input .el-input__prefix) {
  color: #0096ff;
}

/* 验证码输入框 */
.code-input-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-input {
  flex: 1;
  min-width: 0;
}

/* 注册页面验证码输入框占满整行 */
.card-back .code-input-wrapper {
  width: 100%;
  gap: 15px;
}

.card-back .code-input {
  flex: 1;
  min-width: 0;
}

.card-back .code-button {
  flex-shrink: 0;
  width: 140px;
}

.code-button {
  background: linear-gradient(135deg, rgba(0, 150, 255, 0.2), rgba(255, 200, 0, 0.2));
  border: 1px solid rgba(0, 150, 255, 0.5);
  color: #fff;
  transition: all 0.3s;
  min-width: 120px;
  white-space: nowrap;
}

.code-button:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(0, 150, 255, 0.4), rgba(255, 200, 0, 0.4));
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.5);
  transform: translateY(-2px);
}

.code-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  height: 48px;
  margin-top: 15px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0096ff 0%, #00d4ff 100%);
  border: none;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 4px;
  transition: all 0.3s;
}

.submit-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 40px rgba(0, 150, 255, 0.6);
}

.register-button {
  background: linear-gradient(135deg, #ffc800 0%, #ff8c00 100%);
}

.register-button:hover {
  box-shadow: 0 10px 40px rgba(255, 200, 0, 0.6);
}

.button-text {
  position: relative;
  z-index: 2;
}

.button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: buttonGlow 2s infinite;
}

@keyframes buttonGlow {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* 表单底部 */
.form-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-button {
  color: rgba(255, 200, 0, 0.8);
  font-size: 13px;
  transition: all 0.3s;
}

.forgot-button:hover {
  color: #ffc800;
}

.switch-button {
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
}

.switch-button:hover {
  color: #0096ff;
  transform: translateX(5px);
}

/* 下拉选择框样式 */
:deep(.el-select .el-input__wrapper) {
  background: rgba(0, 150, 255, 0.1);
  border: 1px solid rgba(0, 150, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.1);
  transition: all 0.3s;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 150, 255, 0.6);
  box-shadow: 0 0 30px rgba(0, 150, 255, 0.3);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #0096ff;
  box-shadow: 0 0 40px rgba(0, 150, 255, 0.5);
}

:deep(.el-select .el-input__inner) {
  color: #fff;
}

:deep(.el-select .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.el-select .el-select__caret) {
  color: #0096ff;
}

:deep(.el-select-dropdown) {
  background: rgba(15, 30, 50, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 150, 255, 0.3);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5), 0 0 30px rgba(0, 150, 255, 0.3);
}

:deep(.el-select-dropdown__item) {
  color: #fff;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

:deep(.el-select-dropdown__item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, #0096ff, #ffc800);
  transform: scaleY(0);
  transition: transform 0.3s;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(0, 150, 255, 0.2);
  box-shadow: inset 0 0 20px rgba(0, 150, 255, 0.2);
}

:deep(.el-select-dropdown__item:hover::before) {
  transform: scaleY(1);
}

:deep(.el-select-dropdown__item.selected) {
  color: #ffc800;
  background: rgba(0, 150, 255, 0.15);
  font-weight: 600;
}

:deep(.el-select-dropdown__item.selected::before) {
  transform: scaleY(1);
}

:deep(.el-select-dropdown__item.selected::after) {
  content: '✓';
  position: absolute;
  right: 15px;
  color: #ffc800;
  font-size: 18px;
  animation: checkmark 0.3s ease;
}

@keyframes checkmark {
  0% {
    transform: scale(0) rotate(-45deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.2) rotate(0deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}

/* 忘记密码对话框样式 - 模仿登录卡片 */
.forgot-password-dialog {
  --el-dialog-bg-color: transparent;
}

:deep(.forgot-password-dialog .el-dialog) {
  background: rgba(15, 30, 50, 0.85) !important;
  backdrop-filter: blur(20px);
  border-radius: 24px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 40px rgba(0, 150, 255, 0.2),
    inset 0 0 60px rgba(0, 150, 255, 0.05) !important;
  border: 1px solid rgba(0, 150, 255, 0.3) !important;
  overflow: hidden;
}

:deep(.forgot-password-dialog .el-dialog__header) {
  background: transparent !important;
  color: white;
  padding: 30px 30px 20px 30px;
  border-bottom: 1px solid rgba(0, 150, 255, 0.2);
}

:deep(.forgot-password-dialog .el-dialog__title) {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #0096ff 0%, #ffc800 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(0, 150, 255, 0.5);
}

:deep(.forgot-password-dialog .el-dialog__close) {
  color: #0096ff !important;
  font-size: 20px;
  transition: all 0.3s ease;
}

:deep(.forgot-password-dialog .el-dialog__close:hover) {
  color: #00d4ff !important;
  transform: rotate(90deg);
}

:deep(.forgot-password-dialog .el-dialog__body) {
  padding: 30px;
  background: transparent !important;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.forgot-password-dialog .el-dialog__footer) {
  padding: 20px 30px;
  background: transparent !important;
  border-top: 1px solid rgba(0, 150, 255, 0.2);
}

:deep(.forgot-password-dialog .el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

:deep(.forgot-password-dialog .el-form-item) {
  margin-bottom: 22px;
}

/* 忘记密码对话框内所有输入框样式 */
:deep(.forgot-password-dialog .el-input__wrapper) {
  background: rgba(0, 150, 255, 0.1) !important;
  border: 1px solid rgba(0, 150, 255, 0.3) !important;
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.1) !important;
  transition: all 0.3s ease;
}

:deep(.forgot-password-dialog .el-input__wrapper:hover) {
  border-color: rgba(0, 150, 255, 0.6) !important;
  box-shadow: 0 0 30px rgba(0, 150, 255, 0.3) !important;
  background: rgba(0, 150, 255, 0.15) !important;
}

:deep(.forgot-password-dialog .el-input__wrapper.is-focus) {
  border-color: #0096ff !important;
  box-shadow: 0 0 40px rgba(0, 150, 255, 0.5) !important;
  background: rgba(0, 150, 255, 0.15) !important;
}

:deep(.forgot-password-dialog .el-input__inner) {
  color: #0096ff !important;
}

:deep(.forgot-password-dialog .el-input__inner::placeholder) {
  color: rgba(0, 150, 255, 0.5) !important;
}

:deep(.forgot-password-dialog .el-input__prefix) {
  color: #0096ff !important;
}

:deep(.forgot-password-dialog .el-input__suffix) {
  color: #0096ff !important;
}

/* 保留原有的 forgot-input 样式作为备用 */
:deep(.forgot-input .el-input__wrapper) {
  background: rgba(0, 150, 255, 0.1) !important;
  border: 1px solid rgba(0, 150, 255, 0.3) !important;
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.1) !important;
  transition: all 0.3s ease;
}

:deep(.forgot-input .el-input__wrapper:hover) {
  border-color: rgba(0, 150, 255, 0.6) !important;
  box-shadow: 0 0 30px rgba(0, 150, 255, 0.3) !important;
  background: rgba(0, 150, 255, 0.15) !important;
}

:deep(.forgot-input .el-input__wrapper.is-focus) {
  border-color: #0096ff !important;
  box-shadow: 0 0 40px rgba(0, 150, 255, 0.5) !important;
  background: rgba(0, 150, 255, 0.15) !important;
}

:deep(.forgot-input .el-input__inner) {
  color: #0096ff !important;
}

:deep(.forgot-input .el-input__inner::placeholder) {
  color: rgba(0, 150, 255, 0.5) !important;
}

:deep(.forgot-input .el-input__prefix) {
  color: #0096ff !important;
}

.forgot-dialog .code-input-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;
}

.forgot-dialog .code-input {
  flex: 1;
  min-width: 0;
}

.forgot-code-button {
  background: linear-gradient(135deg, #0096ff, #00d4ff);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 120px;
  white-space: nowrap;
}

.forgot-code-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #0080e6, #00bfe6);
  box-shadow: 0 4px 15px rgba(0, 150, 255, 0.4);
  transform: translateY(-2px);
}

.forgot-code-button:disabled {
  background: rgba(0, 150, 255, 0.3);
  color: rgba(255, 255, 255, 0.5);
}

:deep(.forgot-password-dialog .el-button) {
  font-weight: 600;
  transition: all 0.3s ease;
}

.forgot-cancel-btn {
  background: rgba(0, 150, 255, 0.1);
  border: 1px solid rgba(0, 150, 255, 0.3);
  color: #0096ff;
}

.forgot-cancel-btn:hover {
  background: rgba(0, 150, 255, 0.2);
  border-color: #0096ff;
  transform: translateY(-2px);
}

.forgot-submit-btn {
  background: linear-gradient(135deg, #0096ff, #00d4ff);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 150, 255, 0.3);
}

.forgot-submit-btn:hover {
  background: linear-gradient(135deg, #0080e6, #00bfe6);
  box-shadow: 0 6px 20px rgba(0, 150, 255, 0.5);
  transform: translateY(-2px);
}

.forgot-submit-btn.is-loading {
  background: linear-gradient(135deg, #0096ff, #00d4ff);
  opacity: 0.8;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-card {
    width: 90%;
    min-height: auto;
  }

  .card-face {
    padding: 30px 20px;
  }

  .title {
    font-size: 28px;
  }

  .ship-icon {
    font-size: 40px;
  }

  /* 移动端注册表单改为单列 */
  .card-back .auth-form {
    grid-template-columns: 1fr;
  }

  .card-back .auth-form .el-form-item.full-width {
    grid-column: 1;
  }

  .card-back .submit-button {
    grid-column: 1;
  }

  .card-back .form-footer {
    grid-column: 1;
  }
}
</style>
