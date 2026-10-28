<template>
  <el-card class="publish-card" shadow="never">
    <h3>发布笔记</h3>
    <el-form :model="form" label-width="60px">
      <el-form-item label="标题">
        <el-input v-model="form.title" maxlength="64" show-word-limit placeholder="一句话介绍你的分享" />
      </el-form-item>
      <el-form-item label="正文">
        <el-input v-model="form.content" type="textarea" :rows="8" placeholder="分享你的经验、测评或故事..." />
      </el-form-item>
      <el-form-item label="图片">
        <el-upload
          :show-file-list="false"
          accept="image/*"
          :http-request="handleUpload"
        >
          <el-button>上传图片</el-button>
        </el-upload>
        <div v-if="form.coverImage" class="preview">
          <el-image :src="form.coverImage" fit="cover" class="preview-img" />
          <span class="thumb-tip">图片已上传</span>
        </div>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔，如：美食,探店" />
      </el-form-item>
      <el-button type="danger" style="width: 100%" :loading="loading" @click="submit">
        发布笔记
      </el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishNote, uploadImage } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ title: '', content: '', coverImage: '', type: 1, tags: '' })

async function handleUpload({ file }) {
  const formData = new FormData()
  formData.append('file', file)
  const data = await uploadImage(formData)
  // 上传接口返回图片 URL 字符串
  form.coverImage = data
  ElMessage.success('图片上传成功')
}

async function submit() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('标题和正文不能为空')
    return
  }
  loading.value = true
  try {
    const note = await publishNote(form)
    ElMessage.success(note.status === 1 ? '发布成功' : '已保存，请查看审核状态')
    router.push(`/note/${note.id}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.publish-card { max-width: 720px; margin: 0 auto; }
.preview { margin-top: 8px; display: flex; align-items: center; gap: 8px; }
.preview-img { width: 96px; height: 96px; border-radius: 6px; }
.thumb-tip { color: #999; font-size: 12px; }
</style>
