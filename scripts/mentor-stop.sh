#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
docker compose -f "$ROOT/docker-compose.mentor.yml" down
echo "IESST 已停止，演示数据仍保留。"
