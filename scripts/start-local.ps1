param(
    [string]$JavaHome,
    [switch]$SkipBuild,
    [switch]$SkipInfrastructure
)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
New-Item -ItemType Directory -Force logs,.runtime/tmp | Out-Null
$env:TEMP=Join-Path $projectRoot '.runtime/tmp'
$env:TMP=$env:TEMP
$env:npm_config_cache=Join-Path $projectRoot '.runtime/npm-cache'
if(Test-Path '.env.local'){
    foreach($line in Get-Content '.env.local'){
        if($line -match '^([A-Z][A-Z0-9_]*)=(.*)$'){
            [Environment]::SetEnvironmentVariable($Matches[1],$Matches[2].Trim().Trim('"').Trim("'"),'Process')
        }
    }
}
$candidates=@($JavaHome,$env:JAVA_HOME,$env:JAVA_HOME_17_X64)
$jdkDirectory=Join-Path $env:USERPROFILE '.jdks'
if(Test-Path $jdkDirectory){$candidates+=@(Get-ChildItem $jdkDirectory -Directory | Select-Object -ExpandProperty FullName)}
$selected=$null
foreach($candidate in $candidates){
    if(!$candidate){continue}
    $release=Join-Path $candidate 'release'
    if((Test-Path $release) -and ((Get-Content $release) -match '^JAVA_VERSION="17[.]')){
        $selected=$candidate
        break
    }
}
if(!$selected){throw 'JDK 17 not found. Pass -JavaHome with an existing JDK 17 path.'}
$env:JAVA_HOME=$selected
$env:Path=(Join-Path $selected 'bin')+';'+$env:Path
Write-Host ('Using existing JDK 17: '+$selected)
if(!$SkipInfrastructure){
    $plugin='docker/elasticsearch/analysis-ik-8.11.0.zip'
    if(!(Test-Path $plugin)){
        curl.exe -fL --retry 3 https://get.infini.cloud/elasticsearch/analysis-ik/8.11.0 -o $plugin
        if($LASTEXITCODE -ne 0){throw 'IK plugin download failed'}
    }
    $expected='AE32133C324A72379B9FAFA5006C77F4C07A6269D22DF8CC6528A29D09054B00'
    if((Get-FileHash $plugin -Algorithm SHA256).Hash -ne $expected){throw 'IK plugin checksum mismatch'}
    docker compose up -d --wait --wait-timeout 180 mysql redis rabbitmq elasticsearch minio
    if($LASTEXITCODE -ne 0){throw 'Infrastructure startup failed'}

    Get-ChildItem sql/migrations/*.sql | Sort-Object Name | ForEach-Object {
        Get-Content $_.FullName -Raw | docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot community_platform'
        if($LASTEXITCODE -ne 0){throw 'Schema migration failed'}
    }
    if($LASTEXITCODE -ne 0){throw 'Schema migration failed'}
}
$jarPath=Join-Path $projectRoot 'xiaoming-web/target/xiaoming-web-1.0.0.jar'
if(Test-Path '.runtime/backend.pid'){
    $backendPid=[int](Get-Content '.runtime/backend.pid')
    $existing=Get-CimInstance Win32_Process -Filter ("ProcessId="+$backendPid)
    if($existing){
        if($existing.CommandLine -notlike ('*'+$jarPath+'*')){throw 'Saved PID belongs to another process; refusing to stop it'}
        Stop-Process -Id $backendPid
        Wait-Process -Id $backendPid -ErrorAction SilentlyContinue
    }
}
if(!$SkipBuild){
    mvn.cmd -B clean package *> logs/build-local.log
    if($LASTEXITCODE -ne 0){Get-Content logs/build-local.log -Tail 35;throw 'Backend build failed'}
}
if(!(Test-Path $jarPath)){throw 'Backend JAR missing; run without -SkipBuild'}
$backend=Start-Process -FilePath (Join-Path $selected 'bin/java.exe') -ArgumentList @(
    '-Dfile.encoding=UTF-8',
    ('-Djava.io.tmpdir="'+$env:TEMP+'"'),
    '-jar',('"'+$jarPath+'"')
) -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $projectRoot 'logs/backend.log') -RedirectStandardError (Join-Path $projectRoot 'logs/backend-error.log')
$backend.Id | Set-Content '.runtime/backend.pid'
$healthy=$false
for($attempt=0;$attempt -lt 60;$attempt++){
    try{
        $health=Invoke-RestMethod 'http://localhost:8080/api/health' -TimeoutSec 2
        if($health.code -eq 200){$healthy=$true;break}
    }catch{}
    if($backend.HasExited){break}
    Start-Sleep -Seconds 1
}
if(!$healthy){Get-Content logs/backend.log -Tail 30;throw 'Backend startup failed; see logs/backend.log'}
$front=Join-Path $projectRoot 'xiaoming-community/xiaoming-web-vue'
if(!(Test-Path (Join-Path $front 'node_modules/vite/bin/vite.js'))){
    npm.cmd --prefix $front ci --no-audit --no-fund
    if($LASTEXITCODE -ne 0){throw 'Frontend dependency installation failed'}
}
$vite=Join-Path $front 'node_modules/vite/bin/vite.js'
$frontRunning=$false
if(Test-Path '.runtime/frontend.pid'){
    $frontPid=[int](Get-Content '.runtime/frontend.pid')
    $existing=Get-CimInstance Win32_Process -Filter ("ProcessId="+$frontPid)
    if($existing -and $existing.CommandLine -like ('*'+$vite+'*')){$frontRunning=$true}
}
if(!$frontRunning){
    $frontend=Start-Process -FilePath (Get-Command node.exe).Source -ArgumentList @(
        ('"'+$vite+'"'),'--host','127.0.0.1','--strictPort'
    ) -WorkingDirectory $front -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $projectRoot 'logs/frontend.log') -RedirectStandardError (Join-Path $projectRoot 'logs/frontend-error.log')
    $frontend.Id | Set-Content '.runtime/frontend.pid'
}
Write-Host 'Frontend: http://localhost:5173'
Write-Host 'API docs: http://localhost:8080/api/doc.html'
Write-Host 'Logs and temporary files are inside this project on D:.'
