#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STAMP="$(date +%Y%m%d-%H%M%S)"
NAME="IESST-Mentor-Package-$STAMP"
RELEASE_DIR="$ROOT/release"
STAGE="$RELEASE_DIR/$NAME"
ARCHIVE="$RELEASE_DIR/$NAME.zip"

mkdir -p "$RELEASE_DIR"
rm -rf "$STAGE"
mkdir -p "$STAGE"

JAR="$ROOT/server/target/iesst-demo-server-0.1.0.jar"
if [[ ! -f "$JAR" ]]; then
  echo "未找到已构建 JAR，正在构建前后端..."
  "$ROOT/scripts/build-all.sh"
fi

echo "正在整理导师分享包..."
rsync -a "$ROOT/" "$STAGE/" \
  --exclude '.git/' \
  --exclude '.idea/' \
  --exclude '.tools/' \
  --exclude '.env' \
  --exclude '.env.local' \
  --exclude '.env.production' \
  --exclude 'release/' \
  --exclude 'backups/' \
  --exclude 'private-uploads/' \
  --exclude 'uploads/' \
  --exclude 'server/uploads/' \
  --exclude 'client/node_modules/' \
  --exclude 'client/dist/' \
  --exclude 'server/target/' \
  --exclude '*.log' \
  --exclude '.DS_Store'

find "$STAGE" -type f \( -name '*.pdf' -o -name '*.doc' -o -name '*.docx' -o -name '*.key' -o -name '*.pem' \) -delete
chmod +x "$STAGE"/scripts/mentor-*.sh
mkdir -p "$STAGE/mentor-runtime"
cp "$JAR" "$STAGE/mentor-runtime/iesst-app.jar"

cat > "$STAGE/分享包内容说明.txt" <<'EOF'
本包包含完整前端、后端、数据库迁移、演示数据和一键启动配置。
请首先阅读《导师启动说明.md》；安装 Docker Desktop 后即可启动。

为保护用户隐私，本包主动排除了真实环境变量、数据库备份、投稿文件和上传目录。
演示账号：
  管理员 admin / mentor123456
  学生 18800000088 / student123
EOF

(cd "$RELEASE_DIR" && /usr/bin/zip -qr "$ARCHIVE" "$NAME")
rm -rf "$STAGE"

echo "分享包已生成：$ARCHIVE"
echo "大小：$(du -h "$ARCHIVE" | awk '{print $1}')"
