<template>
  <NLayout :has-sider="!isMobile" position="absolute" class="app-layout">
    <!-- Desktop Sidebar -->
    <NLayoutSider
      v-if="!isMobile"
      bordered
      collapse-mode="width"
      :collapsed="appStore.collapsed"
      :collapsed-width="64"
      :width="230"
      show-trigger="bar"
      :native-scrollbar="false"
      class="app-sider"
      @update:collapsed="(val) => (appStore.collapsed = val)"
    >
      <div class="logo-bar" @click="router.push('/')">
        <div class="logo-badge">
          <img src="/favicon.svg" alt="Marry Platform" class="site-logo" />
        </div>
        <div v-show="!appStore.collapsed" class="logo-info">
          <span class="logo-text">marry-platform</span>
          <span class="logo-version">PRO</span>
        </div>
      </div>

      <NMenu
        mode="vertical"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :indent="18"
        :value="activeMenuKey"
        :options="menuOptions"
        @update:value="onMenuSelect"
      />
    </NLayoutSider>

    <!-- Mobile Drawer Sidebar -->
    <NDrawer
      v-if="isMobile"
      v-model:show="mobileDrawerVisible"
      placement="left"
      :width="260"
      class="mobile-nav-drawer"
    >
      <NDrawerContent :native-scrollbar="false" body-content-style="padding: 0;">
        <div class="logo-bar" @click="handleMobileLogoClick">
          <div class="logo-badge">
            <img src="/favicon.svg" alt="Marry Platform" class="site-logo" />
          </div>
          <div class="logo-info">
            <span class="logo-text">marry-platform</span>
            <span class="logo-version">PRO</span>
          </div>
        </div>
        <NMenu
          mode="vertical"
          :indent="18"
          :value="activeMenuKey"
          :options="menuOptions"
          @update:value="onMenuSelect"
        />
      </NDrawerContent>
    </NDrawer>

    <NLayout class="app-main-layout">
      <NLayoutHeader bordered class="header-bar">
        <div class="header-left">
          <NButton
            v-if="isMobile"
            quaternary
            circle
            size="medium"
            class="header-action-btn mobile-menu-btn"
            @click="mobileDrawerVisible = true"
          >
            <NIcon size="20"><MenuOutline /></NIcon>
          </NButton>
          <Breadcrumb class="header-breadcrumb" />
        </div>

        <div class="header-right">
          <NTooltip trigger="hover">
            <template #trigger>
              <NButton quaternary circle size="medium" class="header-action-btn hide-on-mobile" @click="toggleFullScreen">
                <NIcon size="18"><ExpandOutline v-if="!isFullscreen" /><ContractOutline v-else /></NIcon>
              </NButton>
            </template>
            {{ isFullscreen ? '退出全屏' : '全屏显示' }}
          </NTooltip>

          <NTooltip trigger="hover">
            <template #trigger>
              <NPopover trigger="click" placement="bottom-end" :width="280">
                <template #trigger>
                  <NBadge dot color="#6366f1" class="notice-badge">
                    <NButton quaternary circle size="medium" class="header-action-btn">
                      <NIcon size="18"><NotificationsOutline /></NIcon>
                    </NButton>
                  </NBadge>
                </template>
                <div class="notice-box">
                  <div class="notice-header">系统通知</div>
                  <div class="notice-item">
                    <div class="notice-dot" />
                    <div class="notice-content">
                      <div class="notice-title">系统现代化 UI 升级已完成</div>
                      <div class="notice-time">刚刚</div>
                    </div>
                  </div>
                </div>
              </NPopover>
            </template>
            消息通知
          </NTooltip>

          <NTooltip trigger="hover">
            <template #trigger>
              <NButton quaternary circle size="medium" class="header-action-btn" @click="appStore.toggleDark()">
                <NIcon size="18">
                  <Moon v-if="!appStore.dark" />
                  <Sunny v-else />
                </NIcon>
              </NButton>
            </template>
            {{ appStore.dark ? '切换亮色' : '切换暗黑' }}
          </NTooltip>

          <NPopover trigger="hover" placement="bottom-end" :width="200" raw class="user-popover">
            <template #trigger>
              <div class="user-info-btn">
                <NAvatar round size="small" class="user-avatar" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickName?.charAt(0) || 'U' }}
                </NAvatar>
                <span class="user-name hide-on-mobile">{{ userStore.userInfo?.nickName || userStore.userInfo?.username }}</span>
                <NIcon size="14" class="user-arrow hide-on-mobile"><ChevronDownOutline /></NIcon>
              </div>
            </template>

            <div class="user-dropdown-card glass-card">
              <div class="user-dropdown-header">
                <div class="user-dropdown-title">{{ userStore.userInfo?.nickName || userStore.userInfo?.username }}</div>
                <div class="user-dropdown-sub">超级管理员</div>
              </div>
              <div class="user-dropdown-menu">
                <div class="user-dropdown-item" @click="router.push('/')">
                  <NIcon size="16"><HomeOutline /></NIcon>
                  <span>系统首页</span>
                </div>
                <div class="user-dropdown-item danger" @click="handleLogout">
                  <NIcon size="16"><LogOutOutline /></NIcon>
                  <span>退出登录</span>
                </div>
              </div>
            </div>
          </NPopover>
        </div>
      </NLayoutHeader>

      <NLayoutContent class="main-content">
        <RouterView v-slot="{ Component }">
          <Transition name="fade-slide" mode="out-in">
            <KeepAlive>
              <component :is="Component" />
            </KeepAlive>
          </Transition>
        </RouterView>
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, NBadge, NTooltip, NPopover, NDrawer, NDrawerContent } from 'naive-ui'
import {
  SettingsOutline,
  PersonOutline,
  PeopleCircleOutline,
  MenuOutline,
  BusinessOutline,
  BookOutline,
  OptionsOutline,
  ListOutline,
  LogInOutline,
  GlobeOutline,
  CodeSlashOutline,
  NotificationsOutline,
  ExpandOutline,
  ContractOutline,
  HomeOutline,
  LogOutOutline,
  ChevronDownOutline,
  Moon,
  Sunny
} from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import Breadcrumb from './components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const isFullscreen = ref(false)
const isMobile = ref(typeof window !== 'undefined' ? window.innerWidth <= 768 : false)
const mobileDrawerVisible = ref(false)

function checkMobile() {
  if (typeof window !== 'undefined') {
    isMobile.value = window.innerWidth <= 768
  }
}

let mqListener: ((e: MediaQueryListEvent) => void) | null = null
let mediaQueryList: MediaQueryList | null = null

onMounted(() => {
  checkMobile()
  if (typeof window !== 'undefined' && window.matchMedia) {
    mediaQueryList = window.matchMedia('(max-width: 768px)')
    mqListener = (e: MediaQueryListEvent) => {
      isMobile.value = e.matches
    }
    mediaQueryList.addEventListener('change', mqListener)
  }
  window.addEventListener('resize', checkMobile)
  window.addEventListener('orientationchange', checkMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('orientationchange', checkMobile)
  if (mediaQueryList && mqListener) {
    mediaQueryList.removeEventListener('change', mqListener)
  }
})

function handleMobileLogoClick() {
  mobileDrawerVisible.value = false
  router.push('/')
}

function toggleFullScreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

const renderIcon = (icon: any) => () => icon ? h(NIcon, null, { default: () => h(icon) }) : null

const iconMap: Record<string, any> = {
  SettingsOutline,
  PersonOutline,
  PeopleCircleOutline,
  MenuOutline,
  BusinessOutline,
  BookOutline,
  OptionsOutline,
  ListOutline,
  LogInOutline,
  GlobeOutline,
  CodeSlashOutline,

  system: SettingsOutline,
  user: PersonOutline,
  role: PeopleCircleOutline,
  menu: MenuOutline,
  dept: BusinessOutline,
  dict: BookOutline,
  config: OptionsOutline,
  notice: BookOutline,
  log: ListOutline,
  operlog: ListOutline,
  loginlog: LogInOutline,
  online: GlobeOutline,
  job: OptionsOutline,
  gen: CodeSlashOutline,
  monitor: GlobeOutline,
  tool: CodeSlashOutline
}

const menuOptions = computed(() => {
  function convert(menu: any, parentPath = ''): any {
    const fullPath = menu.path ? `${parentPath}/${menu.path}`.replace(/\/+/g, '/') : undefined
    const visibleChildren = (menu.children || [])
      .filter((c: any) => c.menuType !== 'F' && c.visible !== 0)
      .map((c: any) => convert(c, fullPath || ''))
    const option: any = {
      key: String(menu.id),
      label: menu.name,
      icon: renderIcon(iconMap[menu.icon] || MenuOutline),
      path: fullPath
    }
    if (visibleChildren.length) option.children = visibleChildren
    return option
  }
  return permissionStore.sidebarRoutes.map((m: any) => convert(m, ''))
})

const activeMenuKey = computed(() => {
  const currentPath = route.path
  function findKey(options: any[]): string | null {
    for (const opt of options) {
      if (opt.path && opt.path === currentPath) return opt.key
      if (opt.children?.length) {
        const childKey = findKey(opt.children)
        if (childKey) return childKey
      }
    }
    return null
  }
  return findKey(menuOptions.value) || null
})

function onMenuSelect(_key: string, option: any) {
  if (isMobile.value) {
    mobileDrawerVisible.value = false
  }
  if (option.path) router.push(option.path)
}

async function handleLogout() {
  await userStore.logout()
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  background-color: var(--bg-body);
}

.app-sider {
  background-color: var(--bg-card);
  border-right: 1px solid var(--border-soft) !important;
  z-index: 10;
}

.mobile-nav-drawer :deep(.n-drawer-body-content-wrapper) {
  background-color: var(--bg-card);
}

.logo-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 60px;
  padding: 0 16px;
  border-bottom: 1px solid var(--border-soft);
  cursor: pointer;
  user-select: none;
}

.logo-badge {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--bg-hover);
  border: 1px solid var(--border-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-soft);
  flex-shrink: 0;
}

.site-logo {
  width: 28px;
  height: 28px;
  display: block;
}

.logo-info {
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.4px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.logo-version {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 6px;
  background: var(--primary-light);
  color: var(--primary-color);
  letter-spacing: 0.5px;
}

.header-bar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: var(--bg-page-header);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-soft) !important;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-action-btn {
  border-radius: 10px !important;
  color: var(--fg-default);
  transition: all 0.2s ease;
}

.header-action-btn:hover {
  background-color: var(--bg-hover);
  color: var(--primary-color);
}

.user-info-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.user-info-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--border-soft);
}

.user-avatar {
  background: var(--primary-gradient);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--fg-title);
}

.user-arrow {
  color: var(--fg-muted);
  transition: transform 0.2s ease;
}

.user-dropdown-card {
  padding: 8px;
  width: 200px;
}

.user-dropdown-header {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-soft);
  margin-bottom: 6px;
}

.user-dropdown-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--fg-title);
}

.user-dropdown-sub {
  font-size: 12px;
  color: var(--fg-muted);
  margin-top: 2px;
}

.user-dropdown-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--fg-default);
  cursor: pointer;
  transition: all 0.15s ease;
}

.user-dropdown-item:hover {
  background-color: var(--bg-hover);
  color: var(--primary-color);
}

.user-dropdown-item.danger:hover {
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.notice-box {
  padding: 12px 14px;
}

.notice-header {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 10px;
  color: var(--fg-title);
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  background-color: var(--bg-hover);
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  margin-top: 5px;
  flex-shrink: 0;
}

.notice-title {
  font-size: 12px;
  font-weight: 500;
  color: var(--fg-default);
}

.notice-time {
  font-size: 10px;
  color: var(--fg-muted);
  margin-top: 2px;
}

.main-content {
  padding: 20px;
  background-color: var(--bg-main);
  overflow: auto;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.mobile-menu-btn {
  display: none;
}

@media (max-width: 768px) {
  .app-sider {
    display: none !important;
  }
  .mobile-menu-btn {
    display: inline-flex !important;
  }
  .header-bar {
    padding: 0 12px;
    height: 54px;
  }
  .main-content {
    padding: 12px 10px;
  }
  .hide-on-mobile {
    display: none !important;
  }
  .user-info-btn {
    padding: 2px;
  }
}
</style>