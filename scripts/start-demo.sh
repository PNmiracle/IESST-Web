#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
java -jar "$ROOT/server/target/iesst-demo-server-0.1.0.jar"
