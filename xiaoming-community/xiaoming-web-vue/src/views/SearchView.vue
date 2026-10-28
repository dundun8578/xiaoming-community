<template>
  <div>
    <el-card class="search-bar" shadow="never">
      <div class="bar-inner">
        <el-input
          v-model="keyword"
          class="keyword-input"
          placeholder="输入关键词，多个词用空格分隔"
          clearable
          @keyup.enter="doSearch"
        />
        <el-button type="danger" :loading="loading" @click="doSearch">搜索</el-button>
      </div>

    </el-card>

    <el-row :gutter="16">
      <el-col v-for="note in notes" :key="note.id" :xs="12" :sm="8" :md="6" class="card-col">
        <el-card shadow="hover" class="note-card" @click="$router.push(`/note/${note.id}`)">
          <el-image v-if="note.coverImage" :src="note.coverImage" fit="cover" class="cover" />
          <div v-else class="cover placeholder">无封面</div>
          <div class="note-title">{{ note.title }}</div>
          <div class="note-meta"><span>{{ note.authorName }}</span><span>👍 {{ note.likeCount }}</span></div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && searched && notes.length === 0" description="没有找到相关笔记" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchNotes } from '../api'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const notes = ref([])
const loading = ref(false)
const searched = ref(false)

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    notes.value = (await searchNotes({ keyword: keyword.value.trim(), pageNum: 1, pageSize: 20 })) || []
  } finally {
    loading.value = false
  }
}


watch(()=>route.query.keyword,value=>{
  keyword.value=typeof value==='string'?value:''
  if(keyword.value)doSearch()
},{immediate:true})
</script>

<style scoped>
.search-bar { margin-bottom: 16px; }
.bar-inner { display: flex; gap: 12px; align-items: center; }
.keyword-input { flex: 1; }
.mode-tip { color: #999; font-size: 12px; margin-top: 8px; }
.card-col { margin-bottom: 16px; }
.note-card { cursor: pointer; }
.cover { width: 100%; height: 140px; border-radius: 6px; display: block; }
.placeholder { display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #999; font-size: 13px; }
.note-title { font-weight: 600; margin: 8px 0 4px; }
.note-meta { display: flex; justify-content: space-between; color: #999; font-size: 12px; }
</style>
