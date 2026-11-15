@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>&1
if %errorlevel%==0 (
  py -3 -m pip install --user pillow mss || goto :error
  py -3 windows_sender.py
) else (
  where python >nul 2>&1 || goto :missing
  python -m pip install --user pillow mss || goto :error
  python windows_sender.py
)
if errorlevel 1 goto :error
exit /b 0
:missing
echo Python 3 is required. Install it from python.org and run this file again.
pause
exit /b 1
:error
echo Monitor PC failed. Check Wi-Fi and Windows Firewall.
pause
exit /b 1
