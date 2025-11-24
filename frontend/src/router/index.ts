import { createRouter, createWebHistory } from 'vue-router'
const Home = () => import('@/views/HomeView.vue')

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
  ],
})
