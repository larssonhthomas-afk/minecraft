#!/usr/bin/env bash
# Run as root: sudo bash /home/deploy/olinoa-site/setup.sh
set -euo pipefail

DOMAIN="olinoa.com"
SITE_SRC="/home/deploy/olinoa-site"
WEB_ROOT="/var/www/olinoa"

echo "==> Installing nginx and certbot"
apt-get install -y nginx certbot python3-certbot-nginx

echo "==> Creating web root"
mkdir -p "$WEB_ROOT/assets"
cp "$SITE_SRC/index.html" "$WEB_ROOT/"
cp "$SITE_SRC/assets/"* "$WEB_ROOT/assets/"
chown -R www-data:www-data "$WEB_ROOT"
chmod -R 755 "$WEB_ROOT"

echo "==> Writing nginx site config"
cat > /etc/nginx/sites-available/olinoa <<'NGINX'
server {
    listen 80;
    listen [::]:80;
    server_name olinoa.com www.olinoa.com;

    root /var/www/olinoa;
    index index.html;

    location / {
        try_files $uri $uri/ =404;
    }

    # Cache assets
    location /assets/ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header Referrer-Policy no-referrer;
}
NGINX

ln -sf /etc/nginx/sites-available/olinoa /etc/nginx/sites-enabled/olinoa
rm -f /etc/nginx/sites-enabled/default

nginx -t
systemctl enable nginx
systemctl restart nginx

echo ""
echo "==> Obtaining SSL certificate (requires DNS already pointing here)"
certbot --nginx -d "$DOMAIN" -d "www.$DOMAIN" --non-interactive --agree-tos -m larsson.h.thomas@gmail.com --redirect

echo ""
echo "Done! Site live at https://$DOMAIN"
