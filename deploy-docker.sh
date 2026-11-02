#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
command -v docker >/dev/null 2>&1 || { echo '请先安装 Docker Engine 和 Compose 插件。' >&2; exit 1; }
docker compose version >/dev/null
docker compose config --quiet
docker compose build --quiet --pull
docker compose up -d --wait --wait-timeout 60
echo "网站已启动：http://服务器IP:${PORT:-8080}/"
