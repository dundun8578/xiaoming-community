import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/', component: () => import('../views/HomeView.vue') },
  { path: '/search', component: () => import('../views/SearchView.vue') },
  { path: '/publish', component: () => import('../views/PublishView.vue'), meta: { needAuth: true } },
  { path: '/note/:id', component: () => import('../views/NoteDetailView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.needAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
