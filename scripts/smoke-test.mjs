import assert from 'node:assert/strict'
import { setTimeout as delay } from 'node:timers/promises'
import { writeFileSync, mkdirSync } from 'node:fs'
const base=process.env.API_BASE || 'http://localhost:8080/api'
const checks=[]
const notes=[]
let author,reader
async function call(path,{method='GET',body,token,raw=false}={}){
  const headers={}
  if(token)headers.Authorization='Bearer '+token
  if(body && !(body instanceof FormData))headers['Content-Type']='application/json'
  const response=await fetch(base+path,{method,headers,body:body instanceof FormData?body:body?JSON.stringify(body):undefined,signal:AbortSignal.timeout(20000)})
  if(raw)return response
  const result=await response.json()
  assert.equal(response.status,200,path+' HTTP '+response.status)
  return result
}
async function ok(path,options){
  const result=await call(path,options)
  assert.equal(result.code,200,path.split('?')[0]+': '+JSON.stringify(result))
  return result.data
}
function passed(name){checks.push(name);console.log('PASS '+name)}
async function eventually(test,label){
  for(let attempt=0;attempt<50;attempt++){if(await test())return;await delay(500)}
  throw new Error('Timed out: '+label)
}
const suffix=Date.now().toString(36)
try{
  await ok('/health');passed('后端健康检查')
  const page1=await ok('/note/hot?pageNum=1&pageSize=1')
  const page2=await ok('/note/hot?pageNum=2&pageSize=1')
  assert.equal(page1.length,1);assert.equal(page2.length,1);assert.notEqual(page1[0].id,page2[0].id)
  passed('匿名首页与热门列表分页')
  assert.notEqual((await call('/note/hot?pageNum=0&pageSize=1')).code,200)
  passed('分页边界校验')
  const unauthorized=await call('/note/publish',{method:'POST',body:{title:'test'},raw:true})
  assert.equal(unauthorized.status,401);passed('匿名写入被拒绝')
  author=await ok('/user/register',{method:'POST',body:{username:'smoke_a_'+suffix,password:'SmokePass123',nickname:'联调作者'}})
  reader=await ok('/user/register',{method:'POST',body:{username:'smoke_b_'+suffix,password:'SmokePass123',nickname:'联调读者'}})
  author=await ok('/user/login',{method:'POST',body:{username:author.username,password:'SmokePass123'}})
  passed('注册与登录')
  await ok('/user/refresh-token?refreshToken='+encodeURIComponent(author.refreshToken),{method:'POST'})
  assert.notEqual((await call('/user/refresh-token?refreshToken='+encodeURIComponent(author.accessToken),{method:'POST'})).code,200)
  const refreshAsAccess=await call('/user/logout',{method:'POST',token:author.refreshToken,raw:true})
  assert.equal(refreshAsAccess.status,401);passed('双Token刷新与用途隔离')
  const marker='联调'+suffix
  const created=await ok('/note/publish',{method:'POST',token:author.accessToken,body:{title:marker+' 图文分享',content:'社区内容平台真实接口联调',type:1,tags:'联调,社区'}})
  notes.push(created.id)
  assert.equal(created.status,1);assert.equal(created.moderationStatus,'SKIPPED')
  passed('笔记发布与未启用审核状态')
  const invalid=await call('/note/publish',{method:'POST',token:author.accessToken,body:{title:' ',content:'',type:1}})
  assert.notEqual(invalid.code,200);passed('发布内容校验')
  const image=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=','base64')
  const form=new FormData();form.append('file',new Blob([image],{type:'image/png'}),'smoke.png')
  const imageUrl=await ok('/file/upload/image',{method:'POST',token:author.accessToken,body:form})
  assert.match(imageUrl,/^\/api\/file\/content\//)
  const imageResponse=await fetch(new URL(imageUrl,base))
  assert.equal(imageResponse.status,200);assert.deepEqual(Buffer.from(await imageResponse.arrayBuffer()),image)
  passed('MinIO上传与匿名图片读取')
  await ok('/note/'+created.id,{method:'PUT',token:author.accessToken,body:{coverImage:imageUrl}})
  const comment=await ok('/comment/note/'+created.id,{method:'POST',token:reader.accessToken,body:{content:'  评论链路验证  '}})
  assert.equal(comment.content,'评论链路验证');assert.equal(comment.nickname,'联调读者')
  const second=await ok('/comment/note/'+created.id,{method:'POST',token:reader.accessToken,body:{content:'第二条评论'}})
  const comments1=await ok('/comment/note/'+created.id+'?pageNum=1&pageSize=1')
  const comments2=await ok('/comment/note/'+created.id+'?pageNum=2&pageSize=1')
  assert.equal(comments1.length,1);assert.equal(comments2.length,1);assert.notEqual(comments1[0].id,comments2[0].id)
  assert.notEqual((await call('/comment/'+comment.id,{method:'DELETE',token:author.accessToken})).code,200)
  const detail=await ok('/note/'+created.id,{token:reader.accessToken})
  assert.equal(detail.commentCount,2)
  await ok('/comment/'+second.id,{method:'DELETE',token:reader.accessToken})
  passed('评论发布、作者信息、分页、计数与删除权限')
  await ok('/interaction/like/'+created.id,{method:'POST',token:reader.accessToken})
  await ok('/interaction/collect/'+created.id,{method:'POST',token:reader.accessToken})
  const liked=await ok('/note/'+created.id,{token:reader.accessToken})
  assert.equal(liked.isLiked,true);assert.equal(liked.isCollected,true);assert.equal(liked.likeCount,1);assert.equal(liked.collectCount,1)
  const otherDetail=await ok('/note/'+created.id,{token:author.accessToken})
  assert.equal(otherDetail.isLiked,false);assert.equal(otherDetail.isCollected,false)
  passed('点赞收藏状态持久化及用户隔离')
  const before=await ok('/note/'+created.id,{token:reader.accessToken})
  await Promise.all(Array.from({length:10},()=>ok('/note/'+created.id)))
  const after=await ok('/note/'+created.id)
  assert.equal(after.viewCount,before.viewCount+11)
  passed('并发浏览计数不丢失')
  await Promise.all(Array.from({length:10},()=>ok('/interaction/like/'+created.id,{method:'POST',token:reader.accessToken})))
  const parity=await ok('/note/'+created.id,{token:reader.accessToken})
  assert.equal(parity.likeCount,1);assert.equal(parity.isLiked,true)
  passed('并发点赞切换与计数一致')
  assert.notEqual((await call('/note/'+created.id,{method:'PUT',token:reader.accessToken,body:{title:'无权修改'}})).code,200)
  passed('笔记归属权限')
  if(process.env.SKIP_SEARCH!=='1'){
    await eventually(async()=> (await ok('/search/note?keyword='+encodeURIComponent(marker))).some(n=>n.id===created.id),'发布同步ES')
    const updatedMarker=marker+'修改'
    await ok('/note/'+created.id,{method:'PUT',token:author.accessToken,body:{title:updatedMarker,content:'中文分词 搜索 同步验证'}})
    await eventually(async()=> (await ok('/search/note?keyword='+encodeURIComponent(updatedMarker))).some(n=>n.id===created.id),'修改同步ES')
    passed('IK中文搜索与MQ发布、修改同步')
  }
  await ok('/note/'+created.id,{method:'DELETE',token:author.accessToken})
  assert.notEqual((await call('/note/'+created.id)).code,200)
  if(process.env.SKIP_SEARCH!=='1')await eventually(async()=>!(await ok('/search/note?keyword='+encodeURIComponent(marker))).some(n=>n.id===created.id),'删除同步ES')
  passed('笔记逻辑删除与搜索移除')
  await ok('/user/logout',{method:'POST',token:reader.accessToken})
  assert.notEqual((await call('/user/refresh-token?refreshToken='+encodeURIComponent(reader.refreshToken),{method:'POST'})).code,200)
  passed('退出登录使刷新Token失效')
  mkdirSync('logs',{recursive:true})
  writeFileSync('logs/smoke-result.json',JSON.stringify({passed:checks.length,checks,finishedAt:new Date().toISOString()},null,2))
  console.log('All '+checks.length+' integration checks passed.')
}catch(error){console.error(error);process.exitCode=1}
finally{
  if(author)for(const id of notes)try{await call('/note/'+id,{method:'DELETE',token:author.accessToken})}catch{}
}

