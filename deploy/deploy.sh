#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
export SERVICE_VERSION="${1:-0.1}"

command -v mvn >/dev/null 2>&1 || {
  echo "[deploy] Maven 未安装或不在 PATH 中" >&2
  exit 1
}

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "[deploy] 请先安装 Docker Compose" >&2
  exit 1
fi

echo "[deploy] mvn clean package (version=${SERVICE_VERSION})"
mvn -f "${SCRIPT_DIR}/../pom.xml" clean package -DskipTests

echo "[deploy] ${COMPOSE_CMD[*]} up --build -d"
"${COMPOSE_CMD[@]}" -f "${COMPOSE_FILE}" up --build -d

echo "[deploy] ${COMPOSE_CMD[*]} ps"
"${COMPOSE_CMD[@]}" -f "${COMPOSE_FILE}" ps
