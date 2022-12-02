[![Build and Publish](https://github.com/DARPA-ASKEM/TERArium/actions/workflows/publish.yaml/badge.svg?event=push)](https://github.com/DARPA-ASKEM/TERArium/actions/workflows/publish.yaml)
# TERArium

TERArium is the client application for the ASKEM program providing capabilities to create, modify, simulate, and publish
machine extracted models.

## Install and dependencies

The TERArium client is built with Typescript and Vue3. The TERArium server is built with Java and Quarkus. To run and
develop TERArium, you will need these as a prerequisite:

- [Yarn 2](https://yarnpkg.com/getting-started/install)
- [NodeJS 18](https://nodejs.org/en/download/current/)
- [JDK 17](https://openjdk.org/projects/jdk/17/)
- [Quarkus CLI](https://quarkus.io/guides/cli-tooling)

> NOTE: You **must** enable Kubernetes support in Docker. Go to your Docker dashboard -> Settings (Gear icon) ->
> Kubernetes -> Enable Kubernetes
> NOTE: The latest jdk version (i.e., 19) is not supportted and hence the reference to the usage of an earlier version (
> e.g., openjdk17)

### macOS

Installing/Using [Homebrew](https://brew.sh/) to install the following:

* [Temurin](https://adoptium.net/temurin) OR OpenJDK
* [Gradle](https://gradle.org)
* [Quarkus](https://quarkus.io/guides/cli-tooling).

```bash
brew tap homebrew/cask-versions
brew install --cask temurin17 # OR brew install openjdk@17 
brew install gradle
brew install quarkusio/tap/quarkus
```

## Running the server in dev mode

First, launch the authentication service before running the server (see
documentation [here](https://github.com/DARPA-ASKEM/orchestration)).

Then, you can run your application in dev mode that enables live coding using:

```
./gradlew quarkusDev
```

or, if you have the Quarkus CLI

```
quarkus dev
```

> NOTE: Quarkus has a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Running the client in dev mode

To install package dependencies, run the command in the root diretory

```
yarn install
```

### Testing
---

Start local dev server for Vue webapp, with Hot Module Replacement.

```
yarn workspace hmi-client run dev
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

This repository follows the [Conventional Commits Specification](https://conventionalcommits.org/)
using [CommitLint](https://github.com/conventional-changelog/commitlint) to validate the commit message on the PR. If
the message does not conform to the specification the PR will not be allowed to be merged.

This automatic check is done through the use of CI workflows on GitHub defined
in [commitlint.yaml](.github/workflows/commitlint.yaml). It uses the configuration from
the [Commitlint Configuration File](.commitlintrc.yaml).

> Currently the CI configuration is set to check only the PR message as the commits are being squashed. If this ever
> changes and all commits need to be validated then appropriate changes (as commented) in
> the [commitlint.yaml](..github/workflows/commitlint.yaml) should be made.

## License

[Apache License 2.0](LICENSE)
