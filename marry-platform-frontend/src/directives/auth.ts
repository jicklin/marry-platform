import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

type PermValue = string | string[] | undefined | null

/**
 * v-auth="'system:user:add'" or v-auth="['system:user:edit','system:user:remove']"
 *
 * Toggles element visibility via `display: none` instead of removing it from
 * the DOM. That keeps reactive updates working — when the user store's perms
 * change (admin edits roles, multi-tab sync, etc.) the element can be
 * re-shown on next reactive cycle without re-mount cost.
 */
const auth: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<PermValue>) {
    apply(el, binding.value)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<PermValue>) {
    apply(el, binding.value)
  }
}

function apply(el: HTMLElement, value: PermValue) {
  if (!value) {
    el.style.removeProperty('display')
    return
  }
  const ok = hasPerm(value)
  el.style.display = ok ? '' : 'none'
}

/**
 * Programmatic permission check, usable inside template `v-if`:
 *   `<n-button v-if="hasPerm('system:user:edit')">编辑</n-button>`
 */
export function hasPerm(value: string | string[]): boolean {
  const userStore = useUserStore()
  const perms = userStore.perms || []
  return Array.isArray(value)
    ? value.some((p) => perms.includes(p))
    : perms.includes(value)
}

export default auth
