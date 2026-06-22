#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="$ROOT/docker-compose.mentor.yml"

echo "即将删除导师演示版的数据库和上传文件。"
read -r -p "输入 RESET 确认：" answer
if [[ "$answer" != "RESET" ]]; then
  echo "已取消。"
  exit 0
fi

docker compose -f "$COMPOSE" down -v
echo "演示数据已清空。再次运行 ./scripts/mentor-start.sh 会重建数据库。"
