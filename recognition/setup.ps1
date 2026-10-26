param([string]$Python = 'python')
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$runtimePython = Join-Path $projectDir '.venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $runtimePython)) {
    & $Python -m venv (Join-Path $projectDir '.venv')
    if ($LASTEXITCODE -ne 0) { throw 'Python 3.10 or 3.11 is required. Pass -Python with its full path.' }
}
$requirementsFile = Join-Path $PSScriptRoot 'requirements-lock.txt'
if (-not (Test-Path -LiteralPath $requirementsFile)) { $requirementsFile = Join-Path $PSScriptRoot 'requirements.txt' }
& $runtimePython -m pip install -r $requirementsFile
if ($LASTEXITCODE -ne 0) { throw 'Dependency installation failed. Check network access and Python version.' }
& $runtimePython (Join-Path $PSScriptRoot 'download_model.py')
if ($LASTEXITCODE -ne 0) { throw 'Face model setup failed.' }
Write-Output 'Setup complete. Run recognition\start-camera.ps1 to open the local preview.'
