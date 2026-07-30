import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import './router/guard'
import { naive } from './plugins/naive'
import authDirective from './directives/auth'
import { message } from '@/utils/feedback'
import 'vfonts/Lato.css'
import 'vfonts/FiraCode.css'
import './assets/styles/global.scss'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPersist)

app.use(pinia)
app.use(router)
app.use(naive)
app.directive('auth', authDirective)

// Surface uncaught errors / async rejections that aren't handled by
// request.ts's response interceptor (e.g. render bugs, bug in a setter).
app.config.errorHandler = (err: unknown, _vm, info) => {
  // eslint-disable-next-line no-console
  console.error('[vue error]', info, err)
  message.error((err as Error)?.message || '页面发生错误，请刷新重试')
}

app.mount('#app')