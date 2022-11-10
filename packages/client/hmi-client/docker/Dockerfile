FROM nginx:1.23-alpine

# update packages and add some basics
RUN apk update
RUN apk add --no-cache curl && apk add --no-cache bash && apk add --no-cache vim

# remove default and add configuration
COPY default.conf /etc/nginx/conf.d/

# copy the web assets
COPY ./dist /var/www/app

# install wait-for-it
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/81b1373f17855a4dc21156cfe1694c31d7d1792e/wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh
