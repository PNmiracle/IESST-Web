@echo off
chcp 65001 >nul
cd /d "%~dp0"

where docker >nul 2>nul
if errorlevel 1 (
  echo 未检测到 Docker，请先安装并启动 Docker Desktop。
  echo https://www.docker.com/products/docker-desktop/
  pause
  exit /b 1
)

docker info >nul 2>nul
if errorlevel 1 (
  echo Docker Desktop 尚未运行，请启动后重试。
  pause
  exit /b 1
)

echo 正在构建并启动 IESST，首次启动可能需要几分钟...
docker compose -f docker-compose.mentor.yml up -d --build
if errorlevel 1 (
  echo 启动失败。若日志包含 docker.io、timeout 或 DeadlineExceeded，说明 Docker Hub 网络不可用。
  echo 请在 Docker Desktop 的 Settings - Resources - Proxies 中配置网络代理，或更换网络后重试。
  echo 诊断命令：docker pull eclipse-temurin:17-jre
  pause
  exit /b 1
)

echo.
echo 服务正在初始化，请等待约 30-90 秒后访问：
echo 官网：http://127.0.0.1:8080/
echo 后台：http://127.0.0.1:8080/admin
echo 管理员：admin / mentor123456
echo 学生：18800000088 / student123
timeout /t 20 >nul
start http://127.0.0.1:8080/
pause
