import './assets/main.css'

import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'

import { throttle } from './directives/throttle'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 注册全局指令
app.directive('throttle', throttle)

app.mount('#app')
