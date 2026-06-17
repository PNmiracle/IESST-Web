#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MAVEN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"
if [[ ! -x "$MAVEN" ]]; then
  MAVEN="$(command -v mvn || true)"
fi
if [[ -z "$MAVEN" ]]; then
  "$ROOT/scripts/setup.sh"
  MAVEN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"
fi

cd "$ROOT/client"
npm run build

mkdir -p "$ROOT/server/src/main/resources/static"
rm -rf "$ROOT/server/src/main/resources/static/"*
cp -R "$ROOT/client/dist/." "$ROOT/server/src/main/resources/static/"

cd "$ROOT/server"
"$MAVEN" clean package -DskipTests

echo "构建完成：$ROOT/server/target/iesst-demo-server-0.1.0.jar"
