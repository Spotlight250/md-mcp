# Moneydance MCP Release Automator
# Usage: .\release.ps1

$version = Read-Host "Enter release version (e.g., 0.1.0)"
$tagName = "v$version"

Write-Host "`n[1/3] Building and signing Moneydance extension..." -ForegroundColor Cyan
cd plugin
.\gradlew.bat clean signExt
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed!" -ForegroundColor Red; exit }
cd ..

Write-Host "`n[2/4] Packaging AI Automation Skills..." -ForegroundColor Cyan
if (!(Test-Path "skills/dist")) { New-Item -ItemType Directory -Force "skills/dist" }
Remove-Item -Force "skills/dist/*.zip" -ErrorAction SilentlyContinue
cd skills
jar -cMf dist/subscription-finder.zip subscription-finder/
cd ..

Write-Host "`n[3/4] Tagging and pushing source code..." -ForegroundColor Cyan
git add .
git commit -m "release: $tagName"
git tag $tagName
git push origin main --tags

Write-Host "`n[4/4] Creating GitHub Release and uploading assets..." -ForegroundColor Cyan
gh release create $tagName `
    "plugin/dist/mcpserver.mxt#Moneydance Extension (.mxt)" `
    "skills/dist/subscription-finder.zip#Subscription Finder Skill (.zip)" `
    --title "Release $tagName" `
    --notes "Moneydance MCP Bridge Release $tagName"

Write-Host "`n[DONE] Release $tagName is live!" -ForegroundColor Green
Write-Host "View it at: https://github.com/Spotlight250/md-mcp/releases/tag/$tagName"
Write-Host "--------------------------------------------------"
