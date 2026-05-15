# Moneydance MCP Release Automator
# Usage: .\release.ps1

$version = Read-Host "Enter release version (e.g., 0.1.0)"
$tagName = "v$version"

Write-Host "`n[1/4] Building and signing Moneydance extension..." -ForegroundColor Cyan
cd plugin
.\gradlew.bat clean signExt
if ($LASTEXITCODE -ne 0) { Write-Host "`n[ERROR] Build or Signing failed!" -ForegroundColor Red; exit }
cd ..

Write-Host "`n[2/4] Packaging AI Automation Skills..." -ForegroundColor Cyan
if (!(Test-Path "skills/dist")) { New-Item -ItemType Directory -Force "skills/dist" | Out-Null }
Remove-Item -Force "skills/dist/*.zip" -ErrorAction SilentlyContinue
cd skills
jar -cMf dist/subscription-finder.zip subscription-finder/
if ($LASTEXITCODE -ne 0) { Write-Host "`n[ERROR] Skill packaging failed!" -ForegroundColor Red; exit }
cd ..

Write-Host "`n[3/4] Tagging and pushing source code..." -ForegroundColor Cyan
git add .
git commit -m "release: $tagName" --quiet
# We ignore commit failure if there's nothing to commit
git tag $tagName
if ($LASTEXITCODE -ne 0) { Write-Host "`n[ERROR] Tagging failed! (Does $tagName already exist?)" -ForegroundColor Red; exit }
git push origin main --tags
if ($LASTEXITCODE -ne 0) { Write-Host "`n[ERROR] Pushing code/tags failed!" -ForegroundColor Red; exit }

Write-Host "`n[4/4] Creating GitHub Release and uploading assets..." -ForegroundColor Cyan
gh release create $tagName `
    "plugin/dist/mcpserver.mxt#Moneydance Extension (.mxt)" `
    "skills/dist/subscription-finder.zip#Subscription Finder Skill (.zip)" `
    --title "Release $tagName" `
    --notes "### 🚀 Welcome to the Moneydance MCP Bridge ($tagName)`n`nThis release enables AI agents to securely interact with your Moneydance financial data.`n`n#### 📖 Getting Started`n- **[User Guide](https://github.com/Spotlight250/md-mcp/blob/main/docs/user-guide.md)**: Installation and configuration instructions.`n- **[Privacy & Security](https://github.com/Spotlight250/md-mcp/blob/main/docs/user-guide.md#-privacy--security)**: Learn how your data is handled.`n`n#### 📦 Included Assets`n- **mcpserver.mxt**: The Moneydance extension.`n- **subscription-finder.zip**: AI Skill package for auditing recurring payments."

if ($LASTEXITCODE -ne 0) { 
    Write-Host "`n[ERROR] GitHub Release creation failed!" -ForegroundColor Red
    Write-Host "Manual cleanup may be required for tag: $tagName"
    exit 
}

Write-Host "`n[DONE] Release $tagName is live!" -ForegroundColor Green
Write-Host "View it at: https://github.com/Spotlight250/md-mcp/releases/tag/$tagName"
Write-Host "--------------------------------------------------"
