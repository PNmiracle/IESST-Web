#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="$ROOT/docker-compose.mentor.yml"
PORT="${IESST_PORT:-8080}"

if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 Docker。请先安装并启动 Docker Desktop：https://www.docker.com/products/docker-desktop/"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop 尚未运行，请启动后重试。"
  exit 1
fi

echo "正在构建并启动 IESST（首次启动需要下载依赖，请耐心等待）..."
if ! docker compose -f "$COMPOSE" up -d --build; then
  cat <<'EOF'

Docker 构建失败。若日志包含 docker.io、auth.docker.io、timeout 或 DeadlineExceeded，
说明 Docker Hub 下载链路不可用，并非项目代码错误。请：
  1. 确认浏览器能访问 https://hub.docker.com/；
  2. 在 Docker Desktop -> Settings -> Resources -> Proxies 配置可用代理，或更换网络；
  3. 重启 Docker Desktop 后重新运行本脚本。

诊断命令：docker pull eclipse-temurin:17-jre
EOF
  exit 1
fi

echo "正在等待服务就绪..."
for attempt in $(seq 1 60); do
  if curl -fsS "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null | grep -q 'UP'; then
    cat <<EOF

IESST 已启动成功：
  官网：http://127.0.0.1:${PORT}/
  管理后台：http://127.0.0.1:${PORT}/admin
  管理员：admin / mentor123456
  学生演示账号：18800000088 / student123

停止服务：./scripts/mentor-stop.sh
清空演示数据：./scripts/mentor-reset.sh
EOF
    exit 0
  fi
  sleep 3
done

echo "服务未能在预期时间内就绪，最近日志如下："
docker compose -f "$COMPOSE" logs --tail=120 app
exit 1
