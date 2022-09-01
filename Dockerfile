FROM node:18.8.0-slim

# Install git and source code
# RUN apt-get update
# RUN apt-get -y install git
# RUN git clone https://github.com/DARPA-ASKEM/TERArium.git

COPY . TERArium

WORKDIR TERArium

# Install
RUN yarn install
RUN yarn workspace client run build
RUN mkdir -p packages/server/dist/web
RUN cp -r packages/client/dist/* packages/server/dist/web/

# Entry
CMD yarn workspace server run serve
