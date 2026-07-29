import { defineStore } from 'pinia'
import { markRaw, ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { getRouters } from '@/api/system/menu'
import router from '@/router'
import ParentView from '@/layout/components/ParentView.vue'

const modules = import.meta.glob('@/views/**/*.vue')

function loadComponent(component: string): (() => Promise<any>) | null {
  const key = `/src/views/${component}.vue`
  return modules[key] || null
}

function buildRoutes(menus: any[]): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []
  for (const m of menus) {
    // Skip entries that are not navigable at all (buttons / hidden / etc.)
    if (m.menuType === 'F') continue
    if (m.visible === 0) continue

    const hasChildren = Array.isArray(m.children) && m.children.length > 0
    const ownComponent = m.component ? loadComponent(m.component) : null
    // Directory (M-type) entries typically have no component; use ParentView
    // so they can still host their children's <router-view>.
    const isDirectory = m.menuType === 'M' || (!ownComponent && hasChildren)
    const component = ownComponent || (isDirectory ? ParentView : null)
    if (!component) continue

    const route: RouteRecordRaw = {
      path: m.path,
      name: `M${m.id}`,
      component: markRaw(component),
      meta: {
        title: m.name,
        icon: m.icon,
        hidden: m.visible === 0,
        keepAlive: m.isCache === 1
      },
      children: []
    }
    if (hasChildren) {
      const subs: RouteRecordRaw[] = []
      for (const c of m.children) {
        if (c.menuType !== 'C' || c.visible === 0) continue
        const subComp = c.component ? loadComponent(c.component) : null
        if (!subComp) continue
        subs.push({
          path: c.path,
          name: `M${c.id}`,
          component: markRaw(subComp),
          meta: {
            title: c.name,
            icon: c.icon,
            hidden: c.visible === 0,
            keepAlive: c.isCache === 1,
            parentTitle: m.name
          }
        } as RouteRecordRaw)
      }
      if (subs.length) route.children = subs
    }
    result.push(route)
  }
  return result
}

export const usePermissionStore = defineStore('permission', () => {
  const dynamicRoutes = ref<RouteRecordRaw[]>([])
  const sidebarRoutes = ref<any[]>([])
  const loaded = ref<boolean>(false)

  async function generateRoutes() {
    if (loaded.value) return dynamicRoutes.value
    const menus = await getRouters()
    sidebarRoutes.value = menus
    const built = buildRoutes(menus)
    dynamicRoutes.value = built
    // Attach children to the statically-registered Root route so the existing
    // Layout at '/' becomes the parent of all dynamic permission routes.
    for (const route of built) {
      router.addRoute('Root', route)
    }
    loaded.value = true
    return dynamicRoutes.value
  }

  function reset() {
    // Remove the dynamic routes previously attached to the static Root
    // so a subsequent login doesn't accumulate duplicate entries.
    for (const r of dynamicRoutes.value) {
      if (r.name) router.removeRoute(r.name)
    }
    dynamicRoutes.value = []
    sidebarRoutes.value = []
    loaded.value = false
  }

  return { dynamicRoutes, sidebarRoutes, loaded, generateRoutes, reset }
})