param([int]$Camera = 0, [switch]$ListCameras, [ValidateSet('auto','dshow','msmf')][string]$Backend = 'auto')
$runtimePython = Join-Path (Split-Path -Parent $PSScriptRoot) '.venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $runtimePython)) { throw 'Run recognition\setup.ps1 first.' }
$bridgeScript = Join-Path $PSScriptRoot 'webcam_bridge.py'
if ($ListCameras) { & $runtimePython $bridgeScript --list-cameras --backend $Backend }
else { & $runtimePython $bridgeScript --camera $Camera --backend $Backend }
