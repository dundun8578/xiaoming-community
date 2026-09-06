import request from './request'

// ---------- 认证 ----------
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)

// ---------- 笔记 ----------
export const getHotNotes = (params) => request.get('/note/hot', { params })
export const getNoteDetail = (id) => request.get(`/note/${id}`)
export const publishNote = (data) => request.post('/note/publish', data)

// ---------- 搜索 ----------
export const searchNotes = (params) => request.get('/search/note', { params })

// ---------- 互动 ----------
export const toggleLike = (noteId) => request.post(`/interaction/like/${noteId}`)
export const toggleCollect = (noteId) => request.post(`/interaction/collect/${noteId}`)

// ---------- 评论 ----------
export const listComments = (noteId, params) => request.get(`/comment/note/${noteId}`, { params })
export const addComment = (noteId, data) => request.post(`/comment/note/${noteId}`, data)

// ---------- 文件 ----------
export const uploadImage = (formData) =>
  request.post('/file/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
