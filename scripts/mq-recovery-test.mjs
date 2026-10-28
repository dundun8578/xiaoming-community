import assert from 'node:assert/strict'
import { execFileSync } from 'node:child_process'
import { setTimeout as delay } from 'node:timers/promises'
import { writeFileSync } from 'node:fs'
const base='http://localhost:8080/api'
const compose=(...args)=>execFileSync('docker',['compose',...args],{cwd:process.cwd(),stdio:'pipe',timeout:90000})
const sql=query=>compose('exec','-T','mysql','sh','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot -N community_platform -e "$1"','sh',query).toString().trim()
async function ok(url,{method='GET',body,token}={}){
 const response=await fetch(base+url,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},body:body?JSON.stringify(body):undefined,signal:AbortSignal.timeout(20000)})
 const result=await response.json()
 assert.equal(result.code,200,url.split('?')[0]+': '+JSON.stringify(result));return result.data
}
let note,author,reader
try{
 const suffix=Date.now().toString(36)
 author=await ok('/user/register',{method:'POST',body:{username:'mq_a_'+suffix,password:'MqPass123'}})
 reader=await ok('/user/register',{method:'POST',body:{username:'mq_b_'+suffix,password:'MqPass123'}})
 await ok('/follow/'+author.userId,{method:'POST',token:reader.accessToken})
 assert.equal(await ok('/follow/status/'+author.userId,{token:reader.accessToken}),true)
 compose('stop','-t','5','rabbitmq')
 const marker='消息恢复'+suffix
 note=await ok('/note/publish',{method:'POST',token:author.accessToken,body:{title:marker,content:'验证代理中断时保存外发表，恢复后自动同步和通知',type:1}})
 assert.equal(Number(sql('SELECT COUNT(*) FROM note_outbox WHERE note_id='+note.id+' AND sent=0')),1)
 console.log('PASS RabbitMQ停机时发布成功且外发表持久化')
 compose('up','-d','--wait','--wait-timeout','90','rabbitmq')
 let found=false
 for(let i=0;i<90;i++){
   const hits=await ok('/search/note?keyword='+encodeURIComponent(marker))
   const notices=await ok('/message/list?pageSize=100',{token:reader.accessToken})
   if(hits.some(n=>n.id===note.id) && notices.some(n=>n.targetId===note.id)){
     found=true;break
   }
   await delay(1000)
 }
 assert.equal(found,true,'恢复后自动同步与通知')
 assert.equal(Number(sql('SELECT COUNT(*) FROM note_outbox WHERE note_id='+note.id+' AND sent=0')),0)
 console.log('PASS 代理恢复后自动补发、ES可检索、关注者收到通知')
 sql('UPDATE note_outbox SET sent=0, available_at=NOW() WHERE note_id='+note.id)
 await delay(6000)
 const notices=await ok('/message/list?pageSize=100',{token:reader.accessToken})
 assert.equal(notices.filter(n=>n.targetId===note.id).length,1)

 const notice=notices.find(n=>n.targetId===note.id)
 let forbidden=false
 try{await ok('/message/'+notice.id+'/read',{method:'PUT',token:author.accessToken})}catch{forbidden=true}
 assert.equal(forbidden,true)
 await ok('/message/'+notice.id+'/read',{method:'PUT',token:reader.accessToken})
 const read=await ok('/message/list?pageSize=100',{token:reader.accessToken})
 assert.equal(read.find(n=>n.id===notice.id).isRead,1)
 console.log('PASS 消息重复投递不重复生成关注动态，且已读操作校验收件人')
 writeFileSync('logs/mq-recovery-result.json',JSON.stringify({passed:3,brokerRecovered:true,duplicateNotification:false},null,2))
}catch(e){console.error(e);process.exitCode=1}
finally{
 try{compose('up','-d','--wait','--wait-timeout','90','rabbitmq')}catch(e){console.error('RabbitMQ restart failed');process.exitCode=1}
 if(note && author)try{await ok('/note/'+note.id,{method:'DELETE',token:author.accessToken})}catch{}
}
