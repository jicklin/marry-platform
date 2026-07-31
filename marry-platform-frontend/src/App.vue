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

const themeOverrides = computed(() => {
  // Brand / sizing keys are mode-agnostic; text/icon tokens have to flip
  // with the mode or light-mode text would render as white-on-white.
  const text = appStore.dark
    ? {
        // Dark: push everything to the bright end so menu / breadcrumb /
        // card title don't read as muddy grey.
        textColor1: '#ffffff',
        textColor2: 'rgba(255, 255, 255, 0.92)',
        textColor3: 'rgba(255, 255, 255, 0.78)',
        placeholderColor: 'rgba(255, 255, 255, 0.5)',
        iconColor: 'rgba(255, 255, 255, 0.85)',
        iconColorHover: '#ffffff',
        iconColorPressed: 'rgba(255, 255, 255, 0.85)',
        cardTitleTextColor: '#ffffff',
        breadcrumbItemColor: 'rgba(255, 255, 255, 0.78)',
        breadcrumbItemColorHover: '#ffffff',
        breadcrumbSeparatorColor: 'rgba(255, 255, 255, 0.5)'
      }
    : {
        // Light: keep default-contrast text — overriding with white would
        // make text invisible against the white background.
        textColor1: '#303133',
        textColor2: 'rgba(48, 49, 51, 0.9)',
        textColor3: 'rgba(48, 49, 51, 0.65)',
        placeholderColor: 'rgba(48, 49, 51, 0.45)',
        iconColor: 'rgba(48, 49, 51, 0.75)',
        iconColorHover: '#303133',
        iconColorPressed: 'rgba(48, 49, 51, 0.85)',
        cardTitleTextColor: '#303133',
        breadcrumbItemColor: 'rgba(48, 49, 51, 0.7)',
        breadcrumbItemColorHover: '#303133',
        breadcrumbSeparatorColor: 'rgba(48, 49, 51, 0.4)'
      }

  return {
    common: {
      primaryColor: '#2d8cf0',
      primaryColorHover: '#57a3ff',
      primaryColorPressed: '#2379d4',
      primaryColorSuppl: '#2d8cf0',
      // bodyColor / cardColor intentionally NOT overridden — let darkTheme
      // and global.scss CSS variables drive them per mode.
      borderRadius: '6px',
      fontSize: '14px',
      heightMedium: '36px',
      ...text
    },
    Button: {
      textColorPrimary: '#ffffff'
    },
    Menu: {
      itemHeight: '42px'
    },
    Card: {
      paddingMedium: '20px',
      titleTextColor: text.cardTitleTextColor
    },
    Breadcrumb: {
      itemTextColor: text.breadcrumbItemColor,
      itemTextColorHover: text.breadcrumbItemColorHover,
      separatorColor: text.breadcrumbSeparatorColor
    }
  }
})
</script>

<style>
#app-loading { display: none; }
</style>
