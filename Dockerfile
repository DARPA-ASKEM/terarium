FROM node:18.10.0-slim

# Install git and source code
# RUN apt-get update
# RUN apt-get -y install git
# RUN git clone https://github.com/DARPA-ASKEM/TERArium.git

COPY . TERArium

WORKDIR TERArium

# Install
RUN yarn install

# Entry
CMD yarn run serve
