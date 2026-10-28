import { defineStore } from 'pinia'
export const useAuthStore=defineStore('auth',{
  state:()=>({accessToken:localStorage.getItem('accessToken'),nickname:localStorage.getItem('nickname'),userId:localStorage.getItem('userId')}),
  actions:{
    save(data){
      this.userId=data.userId
      localStorage.setItem("userId",String(data.userId))
      this.accessToken=data.accessToken
      this.nickname=data.nickname || data.username
      localStorage.setItem('accessToken',data.accessToken)
      localStorage.setItem('refreshToken',data.refreshToken)
      localStorage.setItem('nickname',this.nickname)
    },
    setAccessToken(token){this.accessToken=token;localStorage.setItem('accessToken',token)},
    clear(){this.accessToken=null;this.nickname=null;this.userId=null
      for(const key of ['accessToken','refreshToken','nickname','userId'])localStorage.removeItem(key)
    }
  }
})
