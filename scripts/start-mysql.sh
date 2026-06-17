#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 Docker。请安装 Docker Desktop，或自行启动 MySQL 8 并创建 iesst 数据库。"
  exit 1
fi

cd "$ROOT"
docker compose up -d mysql
echo "MySQL 正在启动：127.0.0.1:${MYSQL_PORT:-3307}，数据库 iesst，账号 iesst。"
