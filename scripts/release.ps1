# ============================================================
# suretool 发布辅助脚本（Windows PowerShell）
#
# 用法：
#   powershell -ExecutionPolicy Bypass -File scripts\release.ps1 -DryRun      # 本地发布演练（不签名、不上传）
#   powershell -ExecutionPolicy Bypass -File scripts\release.ps1              # 正式发布（需 GPG + OSSRH 已配置）
#   powershell -ExecutionPolicy Bypass -File scripts\release.ps1 -Tag v0.1.0  # 发布成功后打标签并推送
#
# 前置条件（见 docs/RELEASING.md）：
#   1. ~/.m2/settings.xml 已配置 ossrh server + gpg.keyname
#   2. GPG 公钥已上传 keyserver
#   3. JDK 21+（默认 E:\java\jdk21，可用 -JdkHome 覆盖）
# ============================================================
param(
    [switch]$DryRun,
    [string]$Tag = "",
    [string]$JdkHome = "E:\java\jdk21",
    [string]$Mvn = "E:\maven\bin\mvn.cmd"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not (Test-Path $JdkHome)) { Write-Error "JDK 目录不存在: $JdkHome"; exit 1 }
$env:JAVA_HOME = $JdkHome
Write-Host "[1/4] JAVA_HOME=$env:JAVA_HOME"

if ($DryRun) {
    Write-Host "[2/4] 本地发布演练：-Prelease -Dgpg.skip=true verify"
    & $Mvn -Prelease "-Dgpg.skip=true" verify
    if ($LASTEXITCODE -ne 0) { Write-Error "演练失败（EXIT=$LASTEXITCODE）"; exit 1 }
    Write-Host "[3/4] 产物检查："
    Get-ChildItem target\bom.* | Select-Object Name, Length
    Get-ChildItem sure-core\target\sure-core-*.jar | Select-Object -ExpandProperty Name
    Write-Host "[4/4] 演练通过。正式发布前请确认 GPG 密钥与 OSSRH 账号已就绪（docs/RELEASING.md）。"
    exit 0
}

if ($Tag -ne "") {
    Write-Host "[2/4] 打标签并推送: $Tag"
    git tag $Tag
    if ($LASTEXITCODE -ne 0) { Write-Error "打标签失败"; exit 1 }
    git push origin $Tag
    if ($LASTEXITCODE -ne 0) { Write-Error "推送标签失败"; exit 1 }
    Write-Host "[3/4] 标签已推送。请在 GitHub Releases 创建发布并附 CHANGELOG 摘要。"
    exit 0
}

# ---- 正式发布 ----
Write-Host "[2/4] 前置检查：GPG 密钥 与 ~/.m2/settings.xml"
$gpg = Get-Command gpg -ErrorAction SilentlyContinue
if (-not $gpg) {
    $gitGpg = "D:\Program Files\Git\usr\bin\gpg.exe"
    if (Test-Path $gitGpg) { $env:Path += ";D:\Program Files\Git\usr\bin" }
    $gpg = Get-Command gpg -ErrorAction SilentlyContinue
}
if (-not $gpg) { Write-Error "未检测到 gpg 命令。请安装 GnuPG 并生成签名密钥。"; exit 1 }
$settings = Join-Path $env:USERPROFILE ".m2\settings.xml"
if (-not (Test-Path $settings)) {
    Write-Error "缺少 $settings 。请按 docs/RELEASING.md 配置 ossrh server 与 gpg.keyname。"
    exit 1
}
$keys = gpg --list-secret-keys --with-colons 2>$null
if (-not $keys) { Write-Error "GPG 无私钥。请先: gpg --full-generate-key"; exit 1 }

Write-Host "[3/4] 发布到 OSSRH staging（不自动释放）：-Prelease clean deploy"
& $Mvn -Prelease clean deploy
if ($LASTEXITCODE -ne 0) { Write-Error "deploy 失败（EXIT=$LASTEXITCODE）"; exit 1 }

Write-Host "[4/4] 发布成功。下一步（人工，见 docs/RELEASING.md 第 4 节）："
Write-Host "  1. 登录 https://s01.oss.sonatype.org  → Staging Repositories"
Write-Host "  2. 检查制品完整性后 Close（触发签名/坐标校验）"
Write-Host "  3. 校验通过后 Release，等待同步 Maven Central"
Write-Host "  4. 同步完成后运行: powershell scripts\release.ps1 -Tag v0.1.0"
