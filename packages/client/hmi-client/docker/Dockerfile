## Stage 1 : build all the necessary packages
FROM node:18.12.1-alpine as builder
WORKDIR /code
COPY package.json /code
COPY .yarn/ /code/.yarn/
COPY .yarnrc.yml /code
COPY yarn.lock /code
COPY packages/client/graph-scaffolder/package.json /code/packages/client/graph-scaffolder/
COPY packages/client/hmi-client/package.json /code/packages/client/hmi-client/
RUN yarn install

COPY . /code
RUN yarn workspace graph-scaffolder tsc --build
RUN yarn workspace hmi-client build


## Stage 2 : create the docker final image
FROM nginx:1.23-alpine

# update packages and add some basics
RUN apk update
RUN apk add --no-cache curl && apk add --no-cache bash && apk add --no-cache vim

# remove default and add configuration
COPY --from=builder /code/packages/client/hmi-client/docker/default.conf /etc/nginx/conf.d/

# copy the web assets
COPY --from=builder /code/packages/client/hmi-client/dist /var/www/app

# install wait-for-it
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/81b1373f17855a4dc21156cfe1694c31d7d1792e/wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh
