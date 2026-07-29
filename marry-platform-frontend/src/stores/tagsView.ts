import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface TagItem {
  name: string
  path: string
  title: string
  affix?: boolean
}

export const useTagsViewStore = defineStore(
  'tagsView',
  () => {
    const visitedViews = ref<TagItem[]>([])

    function addView(view: TagItem) {
      if (visitedViews.value.some((v) => v.path === view.path)) return
      visitedViews.value.push(view)
    }
    function removeView(path: string) {
      const idx = visitedViews.value.findIndex((v) => v.path === path)
      if (idx >= 0) visitedViews.value.splice(idx, 1)
    }
    function reset() {
      visitedViews.value = []
    }
    return { visitedViews, addView, removeView, reset }
  },
  { persist: false }
)