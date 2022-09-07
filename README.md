# TERArium
TERArium is the client application for the ASKEM program. TERArium provides capabilities to create, modify, simulate, and publish machine extracted models.

## Install and dependencies
TERArium is built with Typescript, Vue3, and Koa backend. To run and develop TERArium, you will need these as a prerequisite:
- [Yarn 2](https://yarnpkg.com/getting-started/install)
- [NodeJS 18](https://nodejs.org/en/download/current/)

To install the package dependencies, run the command in the root diretory

```
yarn install
```


## Dev
Start local dev server for Vue client, with Hot Module Replacement.
```
# Start client
yarn workspace client run dev

# Start server
yarn workspace server run serve

# Start client and server
yarn run dev
```


## Working with Docker
```
# Docker build
docker build . -t <image_name>

# Run, make TERArium available on http://localhost:3000
docker run -p 3000:3000 -ti <image_name>
```
