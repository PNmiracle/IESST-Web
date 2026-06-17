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
cd "$ROOT/server"
"$MAVEN" spring-boot:run
