#!/bin/sh
# Ubuntu / Debian; run: sudo sh deploy-cloud.sh
set -eu
[ "$(id -u)" = 0 ] || { echo 'Please run with sudo.' >&2; exit 1; }
command -v apt-get >/dev/null 2>&1 || { echo 'Requires Ubuntu or Debian.' >&2; exit 1; }
PORT=${PORT:-8080}
case "$PORT" in ''|*[!0-9]*) echo 'Invalid PORT' >&2; exit 1;; esac
[ "$PORT" -ge 1 ] && [ "$PORT" -le 65535 ] || exit 1
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq nginx git
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT HUP INT TERM
git clone --quiet --depth 1 https://github.com/cxze299/deltachat-guide.git "$WORK/repo"
test -f "$WORK/repo/dist/index.html"
SITE=/var/www/deltachat-guide
mkdir -p "$SITE"
cp -R "$WORK/repo/dist/." "$SITE/"
find "$SITE" -type d -exec chmod 755 {} +
find "$SITE" -type f -exec chmod 644 {} +
CONFIG=/etc/nginx/conf.d/deltachat-guide.conf
if [ -e "$CONFIG" ]; then cp "$CONFIG" "$WORK/previous.conf"; fi
cat > "$CONFIG" <<EOF
server {
    listen $PORT;
    server_name _;
    root /var/www/deltachat-guide;
    index index.html;
    location / { try_files \$uri \$uri/ =404; }
}
EOF
if ! nginx -t; then
    if [ -f "$WORK/previous.conf" ]; then
        cp "$WORK/previous.conf" "$CONFIG"
    else
        rm -f "$CONFIG"
    fi
    exit 1
fi
systemctl enable --quiet nginx
if systemctl is-active --quiet nginx; then
    systemctl reload nginx
else
    systemctl start nginx
fi
echo "Ready: http://SERVER_IP:$PORT/"
echo "Allow TCP $PORT in your cloud security group and host firewall."
