import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import './router/guard'
import { naive } from './plugins/naive'
import authDirective from './directives/auth'
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

app.mount('#app')