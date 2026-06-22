#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONTAINER="${MYSQL_CONTAINER:-iesst-mysql}"
DATABASE="${MYSQL_DATABASE:-iesst}"
USER="${MYSQL_USER:-iesst}"
PASSWORD="${MYSQL_PASSWORD:-iesst123}"

"$ROOT/scripts/backup-mysql.sh"
docker exec -i "$CONTAINER" mysql --default-character-set=utf8mb4 -u"$USER" -p"$PASSWORD" "$DATABASE" < "$ROOT/scripts/cleanup-demo-data.sql"
echo "Demo and QA records removed. Review the counts above."
