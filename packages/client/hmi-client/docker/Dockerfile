FROM nginx:1.23-alpine

# update packages and add some basics
RUN apk update \
    && apk add --no-cache curl && apk add --no-cache bash && apk add --no-cache vim

# remove default and add configuration
COPY default.conf /etc/nginx/conf.d/
COPY api-proxy.conf timeouts.conf upstream.conf /etc/nginx/

# copy the web assets
COPY ./dist /var/www
