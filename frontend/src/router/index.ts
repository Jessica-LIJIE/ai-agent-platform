import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/MainLayout.vue'
const Home = () => import('@/views/HomeView.vue')
const PluginList = () => import('@/views/plugin/PluginList.vue')
const AgentList = () => import('@/views/agent/AgentList.vue')
const AgentEditor = () => import('@/views/agent/AgentEditor.vue')
const ChatView = () => import('@/views/chat/ChatView.vue')

export default createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      redirect: '/agents',
      children: [
        {
          path: '/',
          name: 'home',
          component: Home,
          meta: { title: '系统测试' }
        },
        {
          path: '/plugins',
          name: 'plugin-list',
          component: PluginList,
          meta: { title: '插件管理' }
        },
        {
          path: '/agents',
          name: 'agent-list',
          component: AgentList,
          meta: { title: '智能体管理' }
        },
        {
          path: '/agents/:id',
          name: 'agent-editor',
          component: AgentEditor,
          meta: { title: '智能体编辑器' }
        },
        {
          path: '/chat',
          name: 'chat',
          component: ChatView,
          meta: { title: '对话测试' }
        },
      ]
    },
  ],
})
