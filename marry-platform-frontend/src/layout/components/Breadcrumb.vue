<template>
  <NBreadcrumb v-if="items.length" class="custom-breadcrumb">
    <template #separator>
      <NIcon size="14" class="separator-icon"><ChevronForwardOutline /></NIcon>
    </template>
    <NBreadcrumbItem v-for="(it, idx) in items" :key="idx">
      <span v-if="idx === items.length - 1" class="current">{{ it }}</span>
      <span v-else class="ancestor">{{ it }}</span>
    </NBreadcrumbItem>
  </NBreadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { NIcon, NBreadcrumb, NBreadcrumbItem } from 'naive-ui'
import { ChevronForwardOutline } from '@vicons/ionicons5'
import { usePermissionStore } from '@/stores/permission'

const route = useRoute()
const permissionStore = usePermissionStore()

const items = computed<string[]>(() => {
  const segs = route.path.replace(/^\//, '').split('/').filter(Boolean)
  const titles: string[] = []
  for (const s of segs) {
    const top = permissionStore.sidebarRoutes.find((m: any) => m.path === s)
    if (top) {
      titles.push(top.name)
      if (top.children?.length) {
        const sub = top.children.find((c: any) => c.path === s || route.path.includes('/' + s + '/'))
        if (sub) titles.push(sub.name)
      }
      break
    }
  }
  if (titles.length === 0) titles.push('首页')
  return titles
})
</script>

<style scoped>
.custom-breadcrumb {
  display: flex;
  align-items: center;
}

.ancestor {
  color: var(--fg-muted);
  font-weight: 500;
  transition: color 0.15s ease;
}

.ancestor:hover {
  color: var(--fg-title);
}

.current {
  color: var(--primary-color);
  font-weight: 600;
}

.separator-icon {
  color: var(--fg-muted);
  opacity: 0.6;
}
</style>