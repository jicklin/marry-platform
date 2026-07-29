<template>
  <NBreadcrumb separator=">" v-if="items.length">
    <NBreadcrumbItem v-for="(it, idx) in items" :key="idx">
      <span v-if="idx === items.length - 1" class="current">{{ it }}</span>
      <span v-else>{{ it }}</span>
    </NBreadcrumbItem>
  </NBreadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
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
      // try sub
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
.current {
  color: #2d8cf0;
  font-weight: 500;
}
</style>