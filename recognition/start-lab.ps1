param([int]$Camera = 0, [ValidateSet('auto','dshow','msmf')][string]$Backend = 'auto')
$runtimePython = Join-Path (Split-Path -Parent $PSScriptRoot) '.venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $runtimePython)) { throw 'Run recognition\setup.ps1 first.' }
& $runtimePython (Join-Path $PSScriptRoot 'facial_lab.py') --camera $Camera --backend $Backend
