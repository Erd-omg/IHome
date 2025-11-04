import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'
// Disable MockJS - use real backend API
// if (import.meta.env.DEV) {
//   // eslint-disable-next-line @typescript-eslint/ban-ts-comment
//   // @ts-ignore
//   await import('./mocks')
// }

createApp(App)
  .use(router)
  .use(store)
  .use(ElementPlus)
  .mount('#app')


