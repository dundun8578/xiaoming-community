import assert from 'node:assert/strict'
import http from 'node:http'
import { spawn } from 'node:child_process'
import { createWriteStream, existsSync, readdirSync, readFileSync, mkdirSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import os from 'node:os'
import { setTimeout as delay } from 'node:timers/promises'
const root=process.cwd()
const candidates=[process.env.JAVA_HOME,...readdirSync(path.join(os.homedir(),'.jdks')).map(p=>path.join(os.homedir(),'.jdks',p))]
const jdk=candidates.find(p=>p && existsSync(path.join(p,'release')) && /JAVA_VERSION="17[.]/.test(readFileSync(path.join(p,'release'),'utf8')))
assert.ok(jdk,'Existing JDK 17 required')
mkdirSync('.runtime/tmp',{recursive:true});mkdirSync('logs',{recursive:true})
let mode='APPROVE'
let calls=0
const server=http.createServer(async(req,res)=>{
  let raw='';for await(const chunk of req)raw+=chunk
  const body=JSON.parse(raw)
  assert.equal(body.response_format.type,'json_object')
  assert.equal(body.messages[0].role,'system')
  assert.equal(typeof JSON.parse(body.messages[1].content).title,'string')
  calls++
  res.setHeader('Content-Type','application/json')
  if(mode==='FAIL'){res.writeHead(503);return res.end('{}')}
  const content=mode==='MALFORMED'?'not-json':JSON.stringify({decision:mode,reason:'测试审核结果'})
  res.end(JSON.stringify({choices:[{message:{content}}]}))
})
await new Promise((resolve,reject)=>{server.once('error',reject);server.listen(18090,'127.0.0.1',resolve)})
const log=createWriteStream('logs/moderation-backend.log')
const child=spawn(path.join(jdk,'bin','java.exe'),[
  '-Dfile.encoding=UTF-8','-Djava.io.tmpdir='+path.join(root,'.runtime/tmp'),
  '-jar',path.join(root,'xiaoming-web/target/xiaoming-web-1.0.0.jar'),
  '--server.port=18081','--ai.moderation.enabled=true',
  '--ai.moderation.url=http://127.0.0.1:18090/chat/completions',
  '--ai.moderation.api-key=local-test','--ai.moderation.model=fixture',
  '--spring.rabbitmq.listener.simple.auto-startup=false',
  '--logging.level.com.xiaoming=info'
],{cwd:root,windowsHide:true,stdio:['ignore','pipe','pipe']})
child.stdout.pipe(log,{end:false});child.stderr.pipe(log,{end:false})
const base='http://127.0.0.1:18081/api'
let user
const ids=[],checks=[]
async function call(url,{method='GET',body,token}={}){
 const res=await fetch(base+url,{method,headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},body:body?JSON.stringify(body):undefined,signal:AbortSignal.timeout(20000)})
 const result=await res.json();assert.equal(res.status,200);return result
}
try{
 let up=false
 for(let i=0;i<90;i++){
   try{up=(await call('/health')).code===200}catch{}
   if(up)break
   if(child.exitCode!==null)break
   await delay(500)
 }
 assert.equal(up,true,'Moderation test backend startup')
 const registration=await call('/user/register',{method:'POST',body:{username:'audit_'+Date.now().toString(36),password:'AuditPass123'}})
 assert.equal(registration.code,200);user=registration.data
 for(const [decision,status,stored] of [
   ['APPROVE',1,'APPROVE'],['REJECT',4,'REJECT'],['REVIEW',3,'REVIEW'],
   ['MALFORMED',3,'REVIEW'],['FAIL',3,'REVIEW'],['UNKNOWN',3,'REVIEW']
 ]){
   mode=decision
   const result=await call('/note/publish',{method:'POST',token:user.accessToken,body:{title:'审核回归'+decision,content:'审核响应契约测试',type:1}})
   assert.equal(result.code,200);ids.push(result.data.id)
   assert.equal(result.data.status,status);assert.equal(result.data.moderationStatus,stored)
   const own=await call('/note/'+result.data.id,{token:user.accessToken})
   assert.equal(own.code,200);assert.equal(own.data.moderationStatus,stored)
   const publicView=await call('/note/'+result.data.id)
   if(status===1)assert.equal(publicView.code,200)
   else assert.notEqual(publicView.code,200)
   checks.push(decision);console.log('PASS moderation '+decision)
 }
 assert.equal(calls,6)
 writeFileSync('logs/moderation-result.json',JSON.stringify({checks,passed:checks.length,realProviderTested:false},null,2))
 console.log('All 6 moderation contract checks passed; provider was a local fixture.')
}catch(e){console.error(e);process.exitCode=1}
finally{
 if(user)for(const id of ids)try{await call('/note/'+id,{method:'DELETE',token:user.accessToken})}catch{}
 child.kill()
 await Promise.race([new Promise(resolve=>child.once('exit',resolve)),delay(5000)])
 server.closeAllConnections();await new Promise(resolve=>server.close(resolve));log.end()
}
