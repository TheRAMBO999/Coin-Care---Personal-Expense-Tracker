\
Param()
Write-Host "This helper will download and extract a JavaFX SDK zip to the current folder's 'javafx-sdk' directory."
$u = Read-Host "Paste the download URL for JavaFX SDK (Windows x64) and press Enter"
if ([string]::IsNullOrWhiteSpace($u)) {
    Write-Host "No URL provided. Exiting."
    exit 1
}
$zip = "javafx-sdk.zip"
$dest = Join-Path -Path (Get-Location) -ChildPath "javafx-sdk"
Write-Host "Downloading $u ..."
try {
    Invoke-WebRequest -Uri $u -OutFile $zip -UseBasicParsing
} catch {
    Write-Host "Download failed: $_"
    exit 1
}
Write-Host "Extracting..."
if (Test-Path $dest) { Remove-Item -Recurse -Force $dest }
Expand-Archive -LiteralPath $zip -DestinationPath .
# The archive often contains a folder like openjfx-20.0.2_windows-x64_sdk; rename it
$entries = Get-ChildItem -Directory | Where-Object { $_.Name -like "*javafx*" -or $_.Name -like "openjfx*" } 
if ($entries.Count -ge 1) {
    $first = $entries[0]
    Rename-Item -Path $first.FullName -NewName "javafx-sdk" -Force
    Write-Host "JavaFX SDK extracted to: .\\javafx-sdk"
} else {
    Write-Host "Could not find extracted SDK folder. Please extract manually and rename to 'javafx-sdk'."
}
Remove-Item $zip
Write-Host "Done. You can now run 'mvn clean javafx:run' or configure IntelliJ to use module-path .\\javafx-sdk\\lib"
