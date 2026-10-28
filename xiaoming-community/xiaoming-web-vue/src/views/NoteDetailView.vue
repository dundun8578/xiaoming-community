<template>
  <div v-if="note" class="detail">
    <el-row :gutter="16">
      <el-col :md="17" :xs="24">
        <el-card shadow="never">

          <h2 class="title">{{ note.title }}</h2>
          <el-alert v-if="note.moderationStatus" :closable="false"
            :type="note.status === 1 ? 'info' : 'warning'"
            :title="note.moderationReason" style="margin-bottom:12px" />
          <div class="meta">

            <span>{{ note.authorName }}</span>
            <el-button v-if="note.userId && String(note.userId) !== String(auth.userId)" size="small"
              :loading="followLoading" @click="followAuthor">{{ followed ? '取消关注' : '关注作者' }}</el-button>
            <span>{{ formatTime(note.publishTime) }}</span>
            <span>👀 {{ note.viewCount }}</span>
          </div>

          <el-image
            v-if="note.coverImage"
            :src="note.coverImage"
            fit="cover"
            class="cover"
          />
          <video v-if="note.video && note.video.videoUrl" :src="note.video.videoUrl" controls class="video" />
          <div class="content">{{ note.content }}</div>

          <div v-if="imageUrls.length" class="images">
            <el-image
              v-for="(url, i) in imageUrls"
              :key="i"
              :src="url"
              :preview-src-list="imageUrls"
              fit="cover"
              class="content-img"
            />
          </div>

          <div v-if="note.status === 1" class="actions">
            <el-button :type="note.isLiked ? 'danger' : 'default'" round @click="like">
              👍 {{ note.likeCount }}
            </el-button>
            <el-button :type="note.isCollected ? 'warning' : 'default'" round @click="collect">
              ⭐ {{ note.collectCount }}
            </el-button>
          </div>
        </el-card>

        <el-card v-if="note.status === 1" shadow="never" class="comments">
          <h4>评论 {{ note.commentCount }}</h4>
          <div class="comment-input">
            <el-input v-model="commentText" placeholder="说点什么..." @keyup.enter="submitComment" />
            <el-button type="danger" :loading="commentLoading" @click="submitComment">发送</el-button>
          </div>
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-head">
              <span class="comment-author">{{ c.nickname }}</span>
              <span class="comment-time">{{ c.createTime }}</span>
            </div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :md="7" :xs="24">
        <el-card v-if="note.aiSummary" shadow="never" class="ai-card">
          <template #header>
            <span class="ai-title">✨ AI 摘要</span>
          </template>
          <div class="ai-summary">{{ note.aiSummary }}</div>
          <div v-if="aiTagList.length" class="ai-tags">
            <el-tag v-for="t in aiTagList" :key="t" size="small" effect="plain" class="ai-tag">{{ t }}</el-tag>
          </div>
          <div class="ai-note">由大模型自动生成，发布后异步更新</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addComment, getNoteDetail, listComments, toggleCollect, toggleLike, getFollowStatus, toggleFollow } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth=useAuthStore()
const followed=ref(false)
const followLoading=ref(false)
const note = ref(null)
const comments = ref([])
const commentText = ref('')
const commentLoading = ref(false)

const imageUrls = computed(() => (note.value?.images || []).map((img) => img.imageUrl))
const aiTagList = computed(() => (note.value?.aiTags || '').split(',').filter(Boolean))
const isLogin = () => !!localStorage.getItem('accessToken')

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  note.value = await getNoteDetail(route.params.id)
  if(isLogin() && String(note.value.userId)!==String(auth.userId))
    followed.value=await getFollowStatus(note.value.userId)
  comments.value = note.value.status === 1 ? ((await listComments(route.params.id, { pageNum: 1, pageSize: 50 })) || []) : []
}


async function followAuthor(){
  if(!isLogin())return ElMessage.warning('请先登录')
  followLoading.value=true
  try{await toggleFollow(note.value.userId);followed.value=await getFollowStatus(note.value.userId)}
  finally{followLoading.value=false}
}
async function like() {
  if (!isLogin()) return ElMessage.warning('请先登录')
  const data = await toggleLike(note.value.id)
  note.value.isLiked = data.isLiked
  note.value.likeCount += data.isLiked ? 1 : -1
}

async function collect() {
  if (!isLogin()) return ElMessage.warning('请先登录')
  const data = await toggleCollect(note.value.id)
  note.value.isCollected = data.isCollected
  note.value.collectCount += note.value.isCollected ? 1 : -1
}

async function submitComment() {
  if (!isLogin()) return ElMessage.warning('请先登录')
  if (!commentText.value.trim()) return
  commentLoading.value = true
  try {
    const comment = await addComment(note.value.id, { content: commentText.value })
    comments.value.unshift(comment)
    note.value.commentCount += 1
    commentText.value = ''
  } finally {
    commentLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.title { margin: 0 0 8px; }
.meta { display: flex; gap: 16px; color: #999; font-size: 13px; margin-bottom: 16px; }
.cover { width: 100%; max-height: 420px; border-radius: 8px; margin-bottom: 16px; }
.video { width: 100%; border-radius: 8px; margin-bottom: 16px; }
.content { line-height: 1.8; white-space: pre-wrap; }
.images { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.content-img { width: 150px; height: 150px; border-radius: 6px; }
.actions { margin-top: 20px; display: flex; gap: 12px; }
.comments { margin-top: 16px; }
.comment-input { display: flex; gap: 8px; margin-bottom: 16px; }
.comment-item { border-top: 1px solid #f0f0f0; padding: 12px 0; }
.comment-head { display: flex; justify-content: space-between; margin-bottom: 4px; }
.comment-author { font-weight: 600; font-size: 13px; }
.comment-time { color: #bbb; font-size: 12px; }
.comment-content { font-size: 14px; line-height: 1.6; }
.ai-card { border: 1px solid #ede7f6; }
.ai-title { font-weight: 700; background: linear-gradient(90deg,#7c4dff,#e64a3b); -webkit-background-clip: text; color: transparent; }
.ai-summary { font-size: 14px; line-height: 1.7; color: #444; }
.ai-tags { margin-top: 10px; display: flex; gap: 6px; flex-wrap: wrap; }
.ai-tag { color: #7c4dff; }
.ai-note { margin-top: 12px; color: #bbb; font-size: 12px; }
</style>
