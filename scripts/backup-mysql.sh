#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$ROOT/backups}"
CONTAINER="${MYSQL_CONTAINER:-iesst-mysql}"
DATABASE="${MYSQL_DATABASE:-iesst}"
USER="${MYSQL_USER:-iesst}"
PASSWORD="${MYSQL_PASSWORD:-iesst123}"
STAMP="$(date +%Y%m%d-%H%M%S)"
TARGET="$BACKUP_DIR/${DATABASE}-$STAMP.sql.gz"

mkdir -p "$BACKUP_DIR"
docker exec "$CONTAINER" mysqldump -u"$USER" -p"$PASSWORD" --single-transaction --routines --triggers --no-tablespaces "$DATABASE" | gzip > "$TARGET"
echo "MySQL backup created: $TARGET"
