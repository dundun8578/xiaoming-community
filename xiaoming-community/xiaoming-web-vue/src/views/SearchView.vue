<template>
  <div>
    <el-card class="search-bar" shadow="never">
      <div class="bar-inner">
        <el-radio-group v-model="mode">
          <el-radio-button value="keyword">关键词</el-radio-button>
          <el-radio-button value="semantic">语义</el-radio-button>
          <el-radio-button value="hybrid">混合</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          class="keyword-input"
          placeholder="输入关键词，多个词用空格分隔"
          clearable
          @keyup.enter="doSearch"
        />
        <el-button type="danger" :loading="loading" @click="doSearch">搜索</el-button>
      </div>
      <div class="mode-tip">
        <template v-if="mode === 'keyword'">BM25 关键词检索：multi_match + 模糊容错</template>
        <template v-else-if="mode === 'semantic'">kNN 向量检索：按语义相似度召回（需服务端开启 ai.enabled）</template>
        <template v-else>混合检索：BM25 与 kNN 两路召回，归一化加权合并</template>
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
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchNotes } from '../api'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const mode = ref('keyword')
const notes = ref([])
const loading = ref(false)
const searched = ref(false)

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    notes.value = (await searchNotes({ keyword: keyword.value.trim(), mode: mode.value, pageNum: 1, pageSize: 20 })) || []
  } finally {
    loading.value = false
  }
}

// 支持从顶栏搜索框跳转进来
router.afterEach(() => {})
if (route.query.keyword) doSearch()
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
