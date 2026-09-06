<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <span class="logo" @click="$router.push('/')">小明社区</span>
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="搜索笔记，支持语义搜索"
          clearable
          @keyup.enter="goSearch"
        >
          <template #append>
            <el-button @click="goSearch">搜索</el-button>
          </template>
        </el-input>
        <div class="actions">
          <el-button v-if="!token" type="primary" plain @click="$router.push('/login')">登录</el-button>
          <template v-else>
            <el-button type="danger" round @click="$router.push('/publish')">发布</el-button>
            <el-button link @click="logout">退出</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')
const token = ref(localStorage.getItem('accessToken'))

function goSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}

function logout() {
  localStorage.removeItem('accessToken')
  token.value = null
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #f6f6f6; }
.header { background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 10; }
.header-inner { max-width: 1100px; margin: 0 auto; display: flex; align-items: center; gap: 16px; height: 60px; }
.logo { font-size: 20px; font-weight: bold; color: #e64a3b; cursor: pointer; white-space: nowrap; }
.search-input { max-width: 420px; flex: 1; }
.actions { margin-left: auto; display: flex; align-items: center; }
.main { max-width: 1100px; margin: 0 auto; width: 100%; }
</style>
