import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
const request = axios.create({ baseURL: '/api', timeout: 15000 })
let refreshing
function clearSession() {
  useAuthStore().clear()
}
request.interceptors.request.use(config => {
  const token=localStorage.getItem('accessToken')
  if(token) config.headers.Authorization='Bearer '+token
  return config
})
request.interceptors.response.use(response => {
  const result=response.data
  if(result.code!==undefined && result.code!==200){
    ElMessage.error(result.message || '请求失败')
    return Promise.reject(new Error(result.message || '请求失败'))
  }
  return result.data
}, async error => {
  const config=error.config
  const authRequest=/\/user\/(login|register|refresh-token)/.test(config?.url || '')
  if(error.response?.status===401 && !authRequest){
    const token=localStorage.getItem('refreshToken')
    if(token && !config._retried){
      config._retried=true
      try{
        if(!refreshing) refreshing=axios.post('/api/user/refresh-token',null,{
          params:{refreshToken:token},timeout:15000
        }).then(({data})=>{
          if(data.code!==200 || !data.data) throw new Error('登录已过期')
          useAuthStore().setAccessToken(data.data)
          return data.data
        }).finally(()=>{refreshing=null})
        const access=await refreshing
        config.headers.Authorization='Bearer '+access
        return request(config)
      }catch(refreshError){
        clearSession()
        ElMessage.error('登录已过期，请重新登录')
        window.location.assign('/login?redirect='+encodeURIComponent(location.pathname+location.search))
        return Promise.reject(refreshError)
      }
    }
    clearSession()
    ElMessage.error('请先登录')
    window.location.assign('/login?redirect='+encodeURIComponent(location.pathname+location.search))
  }else ElMessage.error(error.response?.data?.message || error.message || '网络异常')
  return Promise.reject(error)
})
export default request
