# !/usr/bin/env bash

envsubst '$${NEXTJS_CONTAINER_IP} $${ACTIX_CONTAINER_IP}'  < /etc/nginx/conf.d/${CONF_FILE} > /etc/nginx/conf.d/default.conf 
rm -rf /etc/nginx/conf.d/dev-nginx.conf
rm -rf /etc/nginx/conf.d/prod-nginx.conf
nginx -g "daemon off;"
