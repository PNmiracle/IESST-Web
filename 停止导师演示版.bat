@echo off
chcp 65001 >nul
cd /d "%~dp0"
docker compose -f docker-compose.mentor.yml down
echo IESST 已停止，演示数据仍保留。
pause
