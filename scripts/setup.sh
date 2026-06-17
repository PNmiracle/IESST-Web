#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TOOLS="$ROOT/.tools"
MAVEN_VERSION="3.9.9"

mkdir -p "$TOOLS"
if [[ ! -x "$TOOLS/apache-maven-$MAVEN_VERSION/bin/mvn" ]]; then
  ARCHIVE="/tmp/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  curl -L "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o "$ARCHIVE"
  tar -xzf "$ARCHIVE" -C "$TOOLS"
fi

cd "$ROOT/client"
npm install

echo "初始化完成。运行 ./scripts/build-all.sh 可重新构建项目。"
