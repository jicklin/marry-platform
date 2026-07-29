declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'vfonts/Lato.css'
declare module 'vfonts/FiraCode.css'