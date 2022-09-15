# TERArium
TERArium is the client application for the ASKEM program. TERArium provides capabilities to create, modify, simulate, and publish machine extracted models.

## Install and dependencies
The TERArium client is built with Typescript and Vue3. The TERArium server is built with Java and Quarkus. To run and develop TERArium, you will need these as a prerequisite:
- [Yarn 2](https://yarnpkg.com/getting-started/install)
- [NodeJS 18](https://nodejs.org/en/download/current/)
- [JDK 17](https://openjdk.org/projects/jdk/17/)
- [Quarkus CLI](https://quarkus.io/guides/cli-tooling)


## Running the server in dev mode

You can run your application in dev mode that enables live coding using:
```
./gradlew quarkusDev
```

or, if you have the Quarkus CLI
```
quarkus dev
```

> **_NOTE:_** Quarkus has a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Running the client in dev mode

To install client package dependencies, run the command in the root diretory
```
yarn install
```

Start local dev server for Vue client, with Hot Module Replacement.
```
yarn workspace client run dev
```


## Packaging and running the server

The application can be packaged using:
```
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/terarium-1.0.0-SNAPSHOT-runner`


## Packaging and running the client


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
