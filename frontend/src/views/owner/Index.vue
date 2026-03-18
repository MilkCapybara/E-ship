<template>
  <div class="owner-container">
    <!-- 背景粒子效果 -->
    <div class="particles-bg"></div>
    <div class="scan-lines"></div>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="logo-glow">
            <span class="logo-icon">⚓</span>
            <h2>E-ship 船东中心</h2>
          </div>
        </div>
        <div class="header-right">
          <div class="user-info">
            <span class="welcome-text">欢迎</span>
            <span class="username">{{ userInfo?.username }}</span>
          </div>
          <el-button @click="logout" class="logout-btn">
            <span>退出登录</span>
          </el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="240px" class="sidebar">
          <div class="sidebar-header">
            <div class="status-indicator"></div>
            <span>导航菜单</span>
          </div>
          <el-menu :default-active="$route.path" router class="nav-menu">
            <el-menu-item index="/owner/dashboard" class="menu-item">
              <el-icon class="menu-icon"><DataAnalysis /></el-icon>
              <span class="menu-text">数据统计</span>
              <div class="item-glow"></div>
            </el-menu-item>
            <el-menu-item index="/owner/ships" class="menu-item">
              <el-icon class="menu-icon"><Ship /></el-icon>
              <span class="menu-text">我的船舶</span>
              <div class="item-glow"></div>
            </el-menu-item>
            <el-menu-item index="/owner/contracts" class="menu-item">
              <el-icon class="menu-icon"><Document /></el-icon>
              <span class="menu-text">合约管理</span>
              <div class="item-glow"></div>
            </el-menu-item>
            <el-menu-item index="/owner/inbox" class="menu-item">
              <el-icon class="menu-icon"><Message /></el-icon>
              <span class="menu-text">合约信箱</span>
              <div class="item-glow"></div>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Ship, Document, Message, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const logout = () => {
  userStore.clearUser()
  router.push('/auth')
}
</script>

<style scoped>
/* 容器基础样式 */
.owner-container {
  height: 100vh;
  background: radial-gradient(ellipse at top, #0a1628 0%, #061627 50%, #030d1a 100%);
  position: relative;
  overflow: hidden;
}

/* 粒子背景效果 */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(2px 2px at 20% 30%, rgba(255, 214, 92, 0.3), transparent),
    radial-gradient(2px 2px at 60% 70%, rgba(79, 168, 255, 0.3), transparent),
    radial-gradient(1px 1px at 50% 50%, rgba(255, 214, 92, 0.2), transparent),
    radial-gradient(1px 1px at 80% 10%, rgba(79, 168, 255, 0.2), transparent),
    radial-gradient(2px 2px at 90% 60%, rgba(255, 214, 92, 0.25), transparent),
    radial-gradient(1px 1px at 33% 80%, rgba(79, 168, 255, 0.25), transparent);
  background-size: 200% 200%;
  background-position: 0% 0%;
  animation: particles-float 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

@keyframes particles-float {
  0%, 100% {
    background-position: 0% 0%;
    opacity: 0.6;
  }
  25% {
    background-position: 100% 50%;
    opacity: 0.8;
  }
  50% {
    background-position: 50% 100%;
    opacity: 1;
  }
  75% {
    background-position: 0% 50%;
    opacity: 0.7;
  }
}

/* 扫描线效果 */
.scan-lines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    0deg,
    rgba(255, 214, 92, 0.03) 0px,
    transparent 1px,
    transparent 2px,
    rgba(255, 214, 92, 0.03) 3px
  );
  pointer-events: none;
  z-index: 2;
  animation: scan-move 8s linear infinite;
}

@keyframes scan-move {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(10px);
  }
}

/* 头部样式 */
.header {
  background: linear-gradient(135deg,
    rgba(6, 22, 39, 0.95) 0%,
    rgba(11, 31, 54, 0.95) 50%,
    rgba(6, 22, 39, 0.95) 100%);
  border-bottom: 2px solid rgba(255, 214, 92, 0.4);
  color: #e9f6ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(255, 214, 92, 0.1),
    inset 0 -1px 0 rgba(255, 214, 92, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 214, 92, 0.6) 50%,
    transparent 100%);
  animation: header-glow 3s ease-in-out infinite;
}

@keyframes header-glow {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-glow {
  display: flex;
  align-items: center;
  gap: 15px;
  animation: logo-pulse 3s ease-in-out infinite;
}

@keyframes logo-pulse {
  0%, 100% {
    filter: drop-shadow(0 0 10px rgba(255, 214, 92, 0.5));
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(255, 214, 92, 0.8));
  }
}

.logo-icon {
  font-size: 32px;
  animation: icon-rotate 10s linear infinite;
}

@keyframes icon-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.header-left h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  background: linear-gradient(135deg, #ffd65c 0%, #ffb703 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(255, 214, 92, 0.3);
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(79, 168, 255, 0.1);
  border: 1px solid rgba(79, 168, 255, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(5px);
}

.welcome-text {
  color: rgba(233, 246, 255, 0.7);
  font-size: 14px;
}

.username {
  color: #ffd65c;
  font-weight: 600;
  font-size: 15px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.logout-btn {
  background: linear-gradient(135deg, rgba(255, 90, 122, 0.2), rgba(255, 90, 122, 0.3));
  border: 1px solid rgba(255, 90, 122, 0.5);
  color: #ff5a7a;
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.logout-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 90, 122, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.logout-btn:hover {
  background: linear-gradient(135deg, rgba(255, 90, 122, 0.3), rgba(255, 90, 122, 0.4));
  border-color: rgba(255, 90, 122, 0.8);
  box-shadow: 0 0 20px rgba(255, 90, 122, 0.4);
  transform: translateY(-2px);
}

.logout-btn:hover::before {
  width: 300px;
  height: 300px;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg,
    rgba(6, 22, 39, 0.9) 0%,
    rgba(11, 31, 54, 0.9) 100%);
  border-right: 2px solid rgba(79, 168, 255, 0.3);
  box-shadow:
    4px 0 20px rgba(0, 0, 0, 0.3),
    inset -1px 0 0 rgba(79, 168, 255, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 5;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(180deg,
    transparent 0%,
    rgba(255, 214, 92, 0.4) 50%,
    transparent 100%);
  animation: sidebar-glow 4s ease-in-out infinite;
}

@keyframes sidebar-glow {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(79, 168, 255, 0.2);
  color: #ffd65c;
  font-weight: 600;
  font-size: 16px;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5);
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffd65c;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.8);
  animation: status-pulse 2s ease-in-out infinite;
}

@keyframes status-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

/* 菜单样式 */
:deep(.nav-menu) {
  background: transparent !important;
  border: none !important;
  padding: 10px;
}

:deep(.menu-item) {
  margin: 8px 0 !important;
  border-radius: 12px !important;
  background: rgba(79, 168, 255, 0.05) !important;
  border: 1px solid rgba(79, 168, 255, 0.2) !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  overflow: hidden !important;
}

:deep(.menu-item:hover) {
  background: rgba(255, 214, 92, 0.15) !important;
  border-color: rgba(255, 214, 92, 0.5) !important;
  box-shadow:
    0 0 20px rgba(255, 214, 92, 0.3),
    inset 0 0 20px rgba(255, 214, 92, 0.1) !important;
  transform: translateX(5px) !important;
}

:deep(.menu-item.is-active) {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.2),
    rgba(255, 183, 3, 0.2)) !important;
  border-color: rgba(255, 214, 92, 0.6) !important;
  box-shadow:
    0 0 25px rgba(255, 214, 92, 0.4),
    inset 0 0 20px rgba(255, 214, 92, 0.15) !important;
}

:deep(.menu-icon) {
  color: #4fa8ff !important;
  font-size: 20px !important;
  transition: all 0.3s ease !important;
}

:deep(.menu-item:hover .menu-icon) {
  color: #ffd65c !important;
  filter: drop-shadow(0 0 8px rgba(255, 214, 92, 0.6)) !important;
  transform: scale(1.1) !important;
}

:deep(.menu-item.is-active .menu-icon) {
  color: #ffd65c !important;
  animation: icon-glow 2s ease-in-out infinite !important;
}

@keyframes icon-glow {
  0%, 100% {
    filter: drop-shadow(0 0 8px rgba(255, 214, 92, 0.6));
  }
  50% {
    filter: drop-shadow(0 0 15px rgba(255, 214, 92, 0.9));
  }
}

:deep(.menu-text) {
  color: rgba(233, 246, 255, 0.8) !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

:deep(.menu-item:hover .menu-text) {
  color: #ffd65c !important;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.5) !important;
}

:deep(.menu-item.is-active .menu-text) {
  color: #ffd65c !important;
  font-weight: 600 !important;
  text-shadow: 0 0 10px rgba(255, 214, 92, 0.6) !important;
}

.item-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 214, 92, 0.2) 50%,
    transparent 100%);
  transition: left 0.6s ease;
}

:deep(.menu-item:hover .item-glow) {
  left: 100%;
}

/* 主内容区域 */
.main-content {
  padding: 20px;
  background: transparent;
  position: relative;
  z-index: 3;
  overflow-y: auto;
}

/* 滚动条美化 */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: rgba(10, 22, 40, 0.5);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.6),
    rgba(79, 168, 255, 0.6));
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(255, 214, 92, 0.4);
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg,
    rgba(255, 214, 92, 0.8),
    rgba(79, 168, 255, 0.8));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }

  .header-left h2 {
    font-size: 20px;
  }

  .logo-icon {
    font-size: 24px;
  }

  .sidebar {
    width: 180px !important;
  }

  .user-info {
    display: none;
  }
}
</style>
