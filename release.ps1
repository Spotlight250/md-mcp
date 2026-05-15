# Moneydance MCP Release Automator
# Usage: .\release.ps1 [-Version 0.2.0] [-Summary "Custom notes"]

param(
    [string]$Version,
    [string]$Summary
)

if (!($Version)) {
    $Version = Read-Host "Enter release version (e.g., 0.1.0)"
}
$tagName = "v$Version"

$changelogPath = "CHANGELOG.md"
if (!(Test-Path $changelogPath)) {
    Write-Host "`n[ERROR] $changelogPath not found!" -ForegroundColor Red
    exit
}

$changelogContent = Get-Content $changelogPath -Raw
# Regex to extract the section for the current version: ## [0.2.0] ... up to the next ## or end of file
$pattern = "(?s)## \[$Version\].*?(?=\n## |$)"
if ($changelogContent -match $pattern) {
    $versionNotes = $Matches[0].Trim()
} else {
    Write-Host "`n[ERROR] Could not find notes for version [$Version] in CHANGELOG.md" -ForegroundColor Red
    exit
}

$finalSummary = if ($Summary) { $Summary } else { $versionNotes }

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
$notes = @"
### 🚀 Welcome to the Moneydance MCP Bridge ($tagName)

This release enables AI agents to securely interact with your Moneydance financial data.

#### 📖 Getting Started
- **[User Guide](https://github.com/Spotlight250/md-mcp/blob/main/docs/user-guide.md)**: Installation and configuration instructions.
- **[Privacy & Security](https://github.com/Spotlight250/md-mcp/blob/main/docs/user-guide.md#-privacy--security)**: Learn how your data is handled.

#### ✨ What's Changed
$finalSummary

---
See the full **[Changelog](https://github.com/Spotlight250/md-mcp/blob/main/CHANGELOG.md)** for details on all versions.

#### 📦 Included Assets
- **mcpserver.mxt**: The Moneydance extension.
- **subscription-finder.zip**: AI Skill package for auditing recurring payments.
"@

gh release create $tagName `
    "plugin/dist/mcpserver.mxt#Moneydance Extension (.mxt)" `
    "skills/dist/subscription-finder.zip#Subscription Finder Skill (.zip)" `
    --title "Release $tagName" `
    --notes $notes

if ($LASTEXITCODE -ne 0) { 
    Write-Host "`n[ERROR] GitHub Release creation failed!" -ForegroundColor Red
    Write-Host "Manual cleanup may be required for tag: $tagName"
    exit 
}

Write-Host "`n[DONE] Release $tagName is live!" -ForegroundColor Green
Write-Host "View it at: https://github.com/Spotlight250/md-mcp/releases/tag/$tagName"
Write-Host "--------------------------------------------------"
