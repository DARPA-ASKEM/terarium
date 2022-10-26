FROM docker-hub.uncharted.software/nginx:1.23-alpine

# update packages and add some basics
RUN apk update
RUN apk add --no-cache curl && apk add --no-cache bash && apk add --no-cache vim

# remove default and add configuration
COPY default.conf /etc/nginx/conf.d/

# copy the web assets
COPY ./dist /var/www/app

# install wait-for-it
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh
