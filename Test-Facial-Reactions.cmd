@echo off
cd /d "%~dp0"
if not exist ".venv\Scripts\python.exe" (
  echo First run the setup instructions in recognition\README.md.
  pause
  exit /b 1
)
".venv\Scripts\python.exe" "recognition\facial_lab.py" %*
if errorlevel 1 pause
