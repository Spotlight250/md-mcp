# Moneydance MCP Release Automator
# Usage: .\release.ps1

$version = Read-Host "Enter release version (e.g., 0.1.0)"
$tagName = "v$version"

Write-Host "`n[1/3] Building and signing Moneydance extension..." -ForegroundColor Cyan
cd plugin
.\gradlew.bat signExt
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
git push origin master --tags

Write-Host "`n[4/4] DONE!" -ForegroundColor Green
Write-Host "--------------------------------------------------"
Write-Host "Your code is pushed and tagged as $tagName."
Write-Host "Final Step: Go to GitHub and upload these files to the release:"
Write-Host "  1. plugin/dist/mcpserver.mxt" -ForegroundColor Yellow
Write-Host "  2. client/src/mcp-proxy.mjs" -ForegroundColor Yellow
Write-Host "  3. skills/dist/subscription-finder.zip" -ForegroundColor Yellow
Write-Host "--------------------------------------------------"
