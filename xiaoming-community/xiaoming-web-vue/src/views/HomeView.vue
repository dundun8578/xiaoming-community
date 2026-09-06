<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="note in notes" :key="note.id" :xs="12" :sm="8" :md="6" class="card-col">
        <el-card shadow="hover" class="note-card" @click="$router.push(`/note/${note.id}`)">
          <el-image
            v-if="note.coverImage"
            :src="note.coverImage"
            fit="cover"
            class="cover"
          >
            <template #error>
              <div class="cover image-placeholder">无封面</div>
            </template>
          </el-image>
          <div v-else class="cover image-placeholder">无封面</div>
          <div class="note-title">{{ note.title }}</div>
          <div class="note-meta">
            <span>{{ note.authorName }}</span>
            <span>👍 {{ note.likeCount }} · ⭐ {{ note.collectCount }}</span>
          </div>
          <div v-if="note.aiSummary" class="ai-badge">AI 摘要</div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && notes.length === 0" description="还没有内容，去发布第一篇笔记吧" />
    <div class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getHotNotes } from '../api'

const notes = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(false)

async function load(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const data = await getHotNotes({ pageNum: page, pageSize })
    notes.value = data || []
    total.value = notes.value.length < pageSize ? (page - 1) * pageSize + notes.value.length : page * pageSize + 1
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))
</script>

<style scoped>
.card-col { margin-bottom: 16px; }
.note-card { cursor: pointer; position: relative; }
.cover { width: 100%; height: 160px; border-radius: 6px; display: block; }
.image-placeholder { display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #999; font-size: 13px; }
.note-title { font-weight: 600; margin: 8px 0 4px; line-height: 1.4; }
.note-meta { display: flex; justify-content: space-between; color: #999; font-size: 12px; }
.ai-badge { position: absolute; top: 8px; right: 8px; background: linear-gradient(90deg,#7c4dff,#e64a3b); color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 10px; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
