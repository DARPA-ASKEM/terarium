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

# Conventional Commits

This repository follows the [Conventional Commits Specification](https://conventionalcommits.org/) using [CommitLint](https://github.com/conventional-changelog/commitlint) to validate the commit message on the PR. If the message does not conform to the specification the PR will not be allowed to be merged.

This automatic check is done through the use of CI workflows on GitHub defined in [commitlint.yml](.github/workflows/commitlint.yml). It uses the configuration from the [Commitlint Configuration File](.commitlintrc.yml). 

> Currently the CI configuration is set to check only the PR message as the commits are being squashed. If this ever changes and all commits need to be validated then appropriate changes (as commented) in the [commitlint.yml](..github/workflows/commitlint.yml) should be made.
