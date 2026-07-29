<template>
  <NLayout has-sider position="absolute" class="app-layout">
    <NLayoutSider
      bordered
      :collapsed="appStore.collapsed"
      :collapsed-width="64"
      :width="220"
      show-trigger
      :native-scrollbar="false"
      @collapse="appStore.collapsed = true"
      @expand="appStore.collapsed = false"
    >
      <div class="logo-bar">
        <NIcon size="22" color="#2d8cf0"><SettingsOutline /></NIcon>
        <span v-show="!appStore.collapsed" class="logo-text">marry-platform</span>
      </div>
      <NMenu
        :collapsed="appStore.collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :indent="18"
        :value="activeMenuKey"
        :options="menuOptions"
        @update:value="onMenuSelect"
      />
    </NLayoutSider>
    <NLayout>
      <NLayoutHeader bordered class="header-bar">
        <Breadcrumb />
        <div class="header-right">
          <NButton quaternary circle @click="appStore.toggleDark()">
            <NIcon size="18"><Moon v-if="!appStore.dark" /><Sunny v-else /></NIcon>
          </NButton>
          <NPopover trigger="hover" placement="bottom-end" :width="200">
            <template #trigger>
              <div class="user-info">
                <NAvatar round size="small" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickName?.charAt(0) || 'U' }}
                </NAvatar>
                <span class="user-name">{{ userStore.userInfo?.nickName || userStore.userInfo?.username }}</span>
              </div>
            </template>
            <NSpace vertical size="small">
              <NButton block @click="router.push('/')">首页</NButton>
              <NButton block @click="handleLogout" type="error">退出登录</NButton>
            </NSpace>
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
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
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
  CodeSlashOutline
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
    // Only attach `children` when there are real sub-items so NMenu doesn't
    // render an expand arrow on what should be a leaf page (e.g. 用户管理).
    if (visibleChildren.length) option.children = visibleChildren
    return option
  }
  return permissionStore.sidebarRoutes.map((m: any) => convert(m, ''))
})

const activeMenuKey = computed(() => {
  const r = route.path.replace(/^\//, '').split('/')
  const top = r[0]
  const item = permissionStore.sidebarRoutes.find((m: any) => m.path === top)
  return item ? String(item.id) : null
})

function onMenuSelect(_key: string, option: any) {
  if (option.path) router.push(option.path)
}

async function handleLogout() {
  await userStore.logout()
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.logo-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.logo-text {
  font-size: 16px;
  background: linear-gradient(135deg, #2d8cf0, #57a3f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.header-bar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.main-content {
  padding: 16px;
  background-color: #f5f7fa;
  overflow: auto;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>