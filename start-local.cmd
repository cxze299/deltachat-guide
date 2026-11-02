@echo off
cd /d "%~dp0"
echo Delta Chat local guide: http://127.0.0.1:8917/
echo Keep this window open. Press Ctrl+C to stop.
python -m http.server 8917 --bind 127.0.0.1 --directory dist
pause
