import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore(
  'app',
  () => {
    const collapsed = ref<boolean>(false)
    const dark = ref<boolean>(false)
    const size = ref<'small' | 'medium' | 'large'>('medium')

    function toggleCollapse() {
      collapsed.value = !collapsed.value
    }
    function toggleDark() {
      dark.value = !dark.value
    }
    function clearCache() {
      /* placeholder for clearing app caches */
 }
    return { collapsed, dark, size, toggleCollapse, toggleDark, clearCache }
  },
  {
    persist: {
      pick: ['collapsed', 'dark', 'size']
    }
  }
)