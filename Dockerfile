FROM node:18.8.0-slim

# Install git and source code
RUN apt-get update
RUN apt-get -y install git
RUN git clone https://github.com/DARPA-ASKEM/TERArium.git

WORKDIR TERArium

# Install
RUN yarn install
RUN yarn workspace client run build

