import type { Plugin } from 'vue'
import {
  create,
  NButton,
  NCard,
  NConfigProvider,
  NDataTable,
  NDatePicker,
  NDialogProvider,
  NDropdown,
  NEmpty,
  NForm,
  NFormItem,
  NGrid,
  NGi,
  NIcon,
  NInput,
  NInputNumber,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NLoadingBarProvider,
  NMenu,
  NMessageProvider,
  NModal,
  NNotificationProvider,
  NPageHeader,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NSpin,
  NStatistic,
  NSwitch,
  NTabPane,
  NTabs,
  NTag,
  NText,
  NTooltip,
  NTree,
  NUpload,
  NDivider,
  NAvatar,
  NBreadcrumb,
  NBreadcrumbItem,
  NScrollbar,
  NDrawer,
  NDrawerContent,
  NCheckbox,
  NRadio,
  NRadioGroup,
  NAlert,
  NCollapse,
  NCollapseItem,
  NList,
  NListItem,
  NThing,
  NPopover,
  NBackTop,
  NEllipsis
} from 'naive-ui'

const naiveUI = create({
  components: [
    NButton, NCard, NConfigProvider, NDataTable, NDatePicker, NDialogProvider,
    NDropdown, NEmpty, NForm, NFormItem, NGrid, NGi, NIcon, NInput, NInputNumber,
    NLayout, NLayoutContent, NLayoutHeader, NLayoutSider, NLoadingBarProvider,
    NMenu, NMessageProvider, NModal, NNotificationProvider, NPageHeader,
    NPagination, NPopconfirm, NSelect, NSpace, NSpin, NStatistic, NSwitch,
    NTabPane, NTabs, NTag, NText, NTooltip, NTree, NUpload, NDivider, NAvatar,
    NBreadcrumb, NBreadcrumbItem, NScrollbar, NDrawer, NDrawerContent,
    NCheckbox, NRadio, NRadioGroup, NAlert, NCollapse, NCollapseItem, NList,
    NListItem, NThing, NPopover, NBackTop, NEllipsis
  ]
})

export const naive: Plugin = {
  install(app) {
    app.use(naiveUI)
  }
}