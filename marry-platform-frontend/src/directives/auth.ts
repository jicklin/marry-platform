import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * v-auth="'system:user:add'" or v-auth="['system:user:edit','system:user:remove']"
 * Removes the element from DOM if the current user lacks the listed permissions.
 */
const auth: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    check(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    check(el, binding)
  }
}

function check(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const value = binding.value
  if (!value) return
  const userStore = useUserStore()
  const perms = userStore.perms || []

  const ok = Array.isArray(value)
    ? value.some((p) => perms.includes(p))
    : perms.includes(value)

  if (!ok && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

export default auth