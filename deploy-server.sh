#!/bin/sh
# 在服务器上从当前 Git 提交发布网站：sh deploy-server.sh [网站目录]
set -eu
repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
target_dir=${1:-/volume1/web/poems/deltachat-guide}
tmp_dir=$(mktemp -d)
trap 'rm -rf "$tmp_dir"' EXIT HUP INT TERM
command -v git >/dev/null
command -v tar >/dev/null
[ -d "$target_dir" ] || { echo "网站目录不存在: $target_dir" >&2; exit 1; }
git -C "$repo_dir" archive HEAD dist | tar -xf - -C "$tmp_dir"
cp -R "$tmp_dir/dist/." "$target_dir/"
echo "部署完成：$target_dir（$(git -C "$repo_dir" rev-parse --short HEAD)）"
