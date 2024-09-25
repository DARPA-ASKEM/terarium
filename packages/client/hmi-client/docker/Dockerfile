FROM nginx:1.23-alpine

# update packages and add some basics
RUN apk update && \
    apk add --no-cache curl=7.88.1-r1 && \
    apk add --no-cache bash=5.2.15-r0 && \
    apk add --no-cache vim=9.0.0999-r0

# remove default and add configuration
COPY default.conf /etc/nginx/conf.d/
COPY api-proxy.conf timeouts.conf upstream.conf /etc/nginx/

# copy the web assets
COPY ./dist /var/www
