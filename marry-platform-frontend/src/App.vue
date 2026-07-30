<template>
  <NConfigProvider
    :theme="appStore.dark ? darkTheme : null"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <NLoadingBarProvider>
      <NDialogProvider>
        <NMessageProvider>
          <NNotificationProvider>
            <RouterView />
          </NNotificationProvider>
        </NMessageProvider>
      </NDialogProvider>
    </NLoadingBarProvider>
  </NConfigProvider>
</template>

<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { darkTheme, zhCN, dateZhCN } from 'naive-ui'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// Mirror dark state to <html class="dark"> so global.scss can switch CSS vars.
watchEffect(() => {
  document.documentElement.classList.toggle('dark', appStore.dark)
})

const themeOverrides = computed(() => ({
  common: {
    primaryColor: '#2d8cf0',
    primaryColorHover: '#57a3f3',
    primaryColorPressed: '#2379d4',
    primaryColorSuppl: '#2d8cf0',
    // bodyColor / cardColor intentionally NOT overridden — let darkTheme
    // and global.scss CSS variables drive them per mode.
    borderRadius: '6px',
    fontSize: '14px',
    heightMedium: '36px'
  },
  Button: {
    textColorPrimary: '#ffffff'
  },
  Menu: {
    itemHeight: '42px'
  },
  Card: {
    paddingMedium: '20px'
  }
}))
</script>

<style>
#app-loading { display: none; }
</style>