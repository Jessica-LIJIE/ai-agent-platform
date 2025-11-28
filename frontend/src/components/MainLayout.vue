<template>
  <div class="main-layout">
    <el-container>
      <!-- 移动端遮罩层 -->
      <div 
        class="mobile-overlay" 
        :class="{ show: isMobileMenuOpen }"
        @click="closeMobileMenu"
      ></div>
      
      <!-- 侧边栏 -->
      <el-aside 
        :width="sidebarCollapsed ? '64px' : '240px'" 
        :class="['new-sidebar', { 
          'collapsed': sidebarCollapsed,
          'mobile-open': isMobileMenuOpen 
        }]"
      >
        <!-- Logo区域 -->
        <div class="new-logo-section">
          <div class="logo-container">
            <div class="logo-icon">
              <el-icon size="28"><ChatDotRound /></el-icon>
            </div>
            <div class="logo-text" v-if="!sidebarCollapsed">
              <h2>AI Agent</h2>
              <span>Platform</span>
            </div>
          </div>
          <div class="logo-divider" v-if="!sidebarCollapsed"></div>
        </div>

        <!-- 导航菜单 -->
        <div class="new-navigation">
          <!-- AI 智能体管理区域 -->
          <div class="nav-section">
            <div class="section-label" v-if="!sidebarCollapsed">AI 智能体</div>
            <div class="nav-items">
              <div 
                class="nav-item"
                :class="{ active: $route.path === '/agents' || $route.path.startsWith('/agents/') }"
                @click="handleNavItemClick('/agents', $event)"
              >
                <div class="item-icon">
                  <el-icon size="20"><ChatDotRound /></el-icon>
                </div>
                <div class="item-content" v-if="!sidebarCollapsed">
                  <span class="item-title">智能体管理</span>
                  <span class="item-desc">创建和管理AI智能体</span>
                </div>
                <div class="item-indicator"></div>
              </div>

              <div 
                class="nav-item"
                :class="{ active: $route.path === '/plugins' }"
                @click="handleNavItemClick('/plugins', $event)"
              >
                <div class="item-icon">
                  <el-icon size="20"><Connection /></el-icon>
                </div>
                <div class="item-content" v-if="!sidebarCollapsed">
                  <span class="item-title">插件管理</span>
                  <span class="item-desc">管理OpenAPI插件</span>
                </div>
                <div class="item-indicator"></div>
              </div>

              <div 
                class="nav-item"
                :class="{ active: $route.path === '/chat' }"
                @click="handleNavItemClick('/chat', $event)"
              >
                <div class="item-icon">
                  <el-icon size="20"><ChatDotSquare /></el-icon>
                </div>
                <div class="item-content" v-if="!sidebarCollapsed">
                  <span class="item-title">对话测试</span>
                  <span class="item-desc">测试智能体对话</span>
                </div>
                <div class="item-indicator"></div>
              </div>
            </div>
          </div>

          <!-- 系统区域 -->
          <div class="nav-section">
            <div class="section-label" v-if="!sidebarCollapsed">系统</div>
            <div class="nav-items">
              <div 
                class="nav-item"
                :class="{ active: $route.path === '/' || $route.path === '/home' }"
                @click="handleNavItemClick('/', $event)"
              >
                <div class="item-icon">
                  <el-icon size="20"><HomeFilled /></el-icon>
                </div>
                <div class="item-content" v-if="!sidebarCollapsed">
                  <span class="item-title">系统测试</span>
                  <span class="item-desc">前后端连通性测试</span>
                </div>
                <div class="item-indicator"></div>
              </div>
            </div>
          </div>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <!-- 移动端菜单按钮 -->
            <button 
              class="mobile-menu-btn"
              @click="toggleMobileMenu"
              v-if="isMobile"
            >
              <el-icon size="20">
                <Menu />
              </el-icon>
            </button>
            
            <el-button 
              type="text" 
              class="sidebar-toggle"
              @click="toggleSidebar"
              v-if="!isMobile"
            >
              <el-icon size="20">
                <component :is="sidebarCollapsed ? 'Expand' : 'Fold'" />
              </el-icon>
            </el-button>
            <div class="page-title">
              <el-icon size="24" :color="pageIcon.color">
                <component :is="pageIcon.icon" />
              </el-icon>
              <h2>{{ pageTitle }}</h2>
            </div>
            <div class="breadcrumb" v-if="!isMobile">
              <span v-for="(item, index) in breadcrumbs" :key="index">
                {{ item }}
                <el-icon v-if="index < breadcrumbs.length - 1"><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
          <div class="header-right">
            <!-- 可以添加用户信息等 -->
          </div>
        </el-header>
        
        <!-- 内容区域 -->
        <el-main class="main-content">
          <div class="content-wrapper">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House, ArrowRight, Fold, Expand, Menu,
  ChatDotRound, Connection, ChatDotSquare, HomeFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const sidebarCollapsed = ref(false)
const isMobileMenuOpen = ref(false)
const windowWidth = ref(window.innerWidth)

// 计算属性
const isMobile = computed(() => windowWidth.value <= 768)

// 页面标题和图标映射
const pageConfig = {
  '/': { title: '系统测试', icon: 'HomeFilled', color: '#409EFF' },
  '/home': { title: '系统测试', icon: 'HomeFilled', color: '#409EFF' },
  '/agents': { title: '智能体管理', icon: 'ChatDotRound', color: '#409EFF' },
  '/plugins': { title: '插件管理', icon: 'Connection', color: '#409EFF' },
  '/chat': { title: '对话测试', icon: 'ChatDotSquare', color: '#67C23A' },
}

// 计算页面标题
const pageTitle = computed(() => {
  const path = route.path
  for (const [key, config] of Object.entries(pageConfig)) {
    if (key === '/' && path === '/') {
      return config.title
    }
    if (key !== '/' && path.startsWith(key)) {
      return config.title
    }
  }
  return '未知页面'
})

// 计算页面图标
const pageIcon = computed(() => {
  const path = route.path
  for (const [key, config] of Object.entries(pageConfig)) {
    if (key === '/' && path === '/') {
      return { icon: config.icon, color: config.color }
    }
    if (key !== '/' && path.startsWith(key)) {
      return { icon: config.icon, color: config.color }
    }
  }
  return { icon: 'House', color: '#409EFF' }
})

// 计算面包屑
const breadcrumbs = computed(() => {
  const path = route.path
  const crumbs = ['首页']
  
  if (path === '/' || path === '/home') {
    crumbs.push('系统测试')
  } else if (path.startsWith('/agents')) {
    crumbs.push('智能体管理')
    if (path.includes('/agents/')) {
      crumbs.push('编辑')
    }
  } else if (path.startsWith('/plugins')) {
    crumbs.push('插件管理')
  } else if (path.startsWith('/chat')) {
    crumbs.push('对话测试')
  }
  
  return crumbs
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 移动端菜单控制
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

// 处理导航项点击
const handleNavItemClick = (routePath, event) => {
  router.push(routePath)
  if (isMobile.value) {
    closeMobileMenu()
  }
}

// 窗口大小监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
  if (isMobile.value) {
    sidebarCollapsed.value = true
    isMobileMenuOpen.value = false
  }
}

// 监听移动端状态变化
watch(isMobile, (newVal) => {
  if (newVal) {
    sidebarCollapsed.value = true
  } else {
    isMobileMenuOpen.value = false
  }
})

// 监听路由变化
watch(route, () => {
  if (isMobile.value) {
    closeMobileMenu()
  }
}, { immediate: true })

// 生命周期钩子
onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 全新现代化设计样式 */
.main-layout {
  height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  overflow: hidden;
}

.main-layout .el-container {
  height: 100%;
}

/* 全新侧边栏设计 - 现代玻璃态风格 */
.new-sidebar {
  height: 100vh;
  background: linear-gradient(180deg, 
    rgba(15, 23, 42, 0.95) 0%, 
    rgba(30, 41, 59, 0.95) 50%, 
    rgba(51, 65, 85, 0.95) 100%);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.new-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(147, 51, 234, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(16, 185, 129, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.new-sidebar.collapsed {
  width: 64px !important;
}

/* Logo区域设计 */
.new-logo-section {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  position: relative;
  flex-shrink: 0;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 8px 32px rgba(59, 130, 246, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: logoRotate 4s linear infinite;
}

@keyframes logoRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.logo-icon .el-icon {
  color: #ffffff;
  position: relative;
  z-index: 1;
}

.logo-text h2 {
  margin: 0;
  color: #ffffff;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #ffffff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-text span {
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.logo-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
  margin-top: 16px;
  opacity: 0.6;
}

/* 导航菜单设计 */
.new-navigation {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
  min-height: 0;
}

.new-navigation::-webkit-scrollbar {
  display: none;
}

.nav-section {
  margin-bottom: 32px;
}

.section-label {
  color: #64748b;
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin: 0 12px 10px 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(100, 116, 139, 0.2);
  position: relative;
}

.section-label::before {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 24px;
  height: 1px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 12px;
}

/* 导航项目设计 */
.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  background: rgba(51, 65, 85, 0.3);
  border: 1px solid rgba(148, 163, 184, 0.1);
  backdrop-filter: blur(10px);
  margin-bottom: 4px;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.1) 0%, 
    rgba(147, 51, 234, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.item-icon {
  width: 36px;
  height: 36px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.item-icon .el-icon {
  color: #60a5fa;
  transition: all 0.3s ease;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  position: relative;
  z-index: 2;
}

.item-title {
  color: #e2e8f0;
  font-size: 0.875rem;
  font-weight: 600;
  transition: color 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  color: #94a3b8;
  font-size: 0.7rem;
  font-weight: 400;
  transition: color 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-indicator {
  width: 4px;
  height: 24px;
  background: transparent;
  border-radius: 2px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

/* 悬停效果 */
.nav-item:hover {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.2) 0%, 
    rgba(147, 51, 234, 0.2) 100%);
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateX(8px) scale(1.02);
  box-shadow: 
    0 12px 40px rgba(59, 130, 246, 0.2),
    0 0 0 1px rgba(59, 130, 246, 0.1);
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item:hover .item-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  transform: scale(1.1);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
}

.nav-item:hover .item-icon .el-icon {
  color: #ffffff;
  transform: scale(1.1);
}

.nav-item:hover .item-title {
  color: #ffffff;
}

.nav-item:hover .item-desc {
  color: #cbd5e1;
}

.nav-item:hover .item-indicator {
  background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 100%);
}

/* 激活状态 */
.nav-item.active {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.25) 0%, 
    rgba(147, 51, 234, 0.25) 100%);
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateX(6px);
  box-shadow: 
    0 16px 48px rgba(59, 130, 246, 0.25),
    0 0 0 1px rgba(59, 130, 246, 0.2);
}

.nav-item.active::before {
  opacity: 1;
}

.nav-item.active .item-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
}

.nav-item.active .item-icon .el-icon {
  color: #ffffff;
}

.nav-item.active .item-title {
  color: #ffffff;
  font-weight: 700;
}

.nav-item.active .item-desc {
  color: #cbd5e1;
}

.nav-item.active .item-indicator {
  background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 100%);
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.6);
}

/* 折叠状态样式 */
.new-sidebar.collapsed .logo-text,
.new-sidebar.collapsed .section-label,
.new-sidebar.collapsed .item-content {
  display: none;
}

.new-sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 16px;
}

.new-sidebar.collapsed .item-icon {
  margin-right: 0;
}

.new-sidebar.collapsed .logo-container {
  justify-content: center;
}

/* 头部样式 */
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background: #f1f5f9;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title h2 {
  margin: 0;
  color: #1e293b;
  font-size: 1.5rem;
  font-weight: 600;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.9rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 主内容区域 */
.main-content {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

/* 移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 0, 0, 0.7) 0%, 
    rgba(30, 41, 59, 0.8) 100%);
  backdrop-filter: blur(8px);
  z-index: 999;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.mobile-overlay.show {
  opacity: 1;
  visibility: visible;
}

.mobile-menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border: 1px solid #e2e8f0;
  color: #475569;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.mobile-menu-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: #ffffff;
  border-color: #60a5fa;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  transform: scale(1.05);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .new-sidebar {
    width: 220px;
  }
  
  .new-sidebar.collapsed {
    width: 64px;
  }
  
  .nav-item {
    padding: 14px 16px;
  }
  
  .item-icon {
    width: 40px;
    height: 40px;
  }
  
  .main-content {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .new-sidebar {
    position: fixed;
    left: -240px;
    top: 0;
    height: 100vh;
    width: 280px !important;
    z-index: 1000;
    transition: left 0.3s ease;
  }
  
  .new-sidebar.mobile-open {
    left: 0;
  }
  
  .new-sidebar.collapsed {
    width: 240px !important;
  }
  
  .header {
    padding: 0 16px;
    height: 56px;
  }
  
  .mobile-menu-btn {
    display: flex !important;
  }
  
  .page-title h2 {
    font-size: 1.1rem;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .main-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 12px;
    height: 52px;
  }
  
  .page-title h2 {
    font-size: 1rem;
  }
  
  .main-content {
    padding: 12px;
  }
}

/* 过渡动画 */
.new-sidebar {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-content {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.mobile-overlay {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 滚动条样式 */
.new-navigation::-webkit-scrollbar {
  width: 4px;
}

.new-navigation::-webkit-scrollbar-track {
  background: transparent;
}

.new-navigation::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 2px;
}

.new-navigation::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}
</style>

