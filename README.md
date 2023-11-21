
# Terarium

Terarium is the client application for the ASKEM program providing capabilities to create, modify, simulate, and publish
machine extracted models.

[app.terarium.ai](https://app.terarium.ai/)

## Table of Contents

1. [Project Status](#project-status)
1. [Getting Started](#getting-started)
  1. [Dependencies](#dependencies)
  1. [Running and Debugging](#Running and Debugging)
     1. [Running the Client](#running-the-client)
  1. [Running Tests](#running-tests)
     1. [Other Tests](#other-tests)
  1. [Installation](#installation)
  1. [Usage](#usage)
1. [Release Process](#release-process)
  1. [Versioning](#versioning)
  1. [Payload](#payload)
1. [How to Get Help](#how-to-get-help)
1. [Contributing](#contributing)
1. [Further Reading](#further-reading)
1. [License](#license)
1. [Authors](#authors)
1. [Acknowledgments](#acknowledgements)


## Project Status
[![Build and Publish](https://github.com/DARPA-ASKEM/Terarium/actions/workflows/publish.yaml/badge.svg?event=push)](https://github.com/DARPA-ASKEM/TERArium/actions/workflows/publish.yaml)


[![Client E2E Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client-e2e.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client-e2e.yaml)
[![Client Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client.yaml)
[![Server Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-server.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-server.yaml)

## Getting Started

### Dependencies

The Terarium client is built with Typescript and Vue3. The Terarium server is built with Java and Spring Boot. To run and
develop Terarium, you will need these as a prerequisite:

- [Yarn 2](https://yarnpkg.com/getting-started/install)
- [NodeJS 18](https://nodejs.org/en/download/current/)
- [JDK 17](https://adoptium.net/temurin)
- [Gradle 7](https://gradle.org/install/)

There are many ways/package managers to install these dependencies. We recommend using [Homebrew](https://brew.sh/) on MacOS. 

```bash
brew tap homebrew/cask-versions
brew install --cask temurin17 # OR brew install openjdk@17 
brew install gradle
brew install yarn
```

In addition, you will need to have the ansible askem vault id file in your home directory. This is used to decrypt the local secrets file. This file is not included in the repository for security reasons. Please contact the team for access to this file.

### Running and Debugging
There is a companion project to Terarium which handles spinning up the required services. Depending on what you're doing this can be configured to run all or some of the related services. You'll need to start the orchestration project up before beginning (see documentation [here](https://github.com/DARPA-ASKEM/orchestration)).


#### Running the Client and Server
To install package dependencies, run the command in the root directory

```shell
yarn install
```

If you don't intend to work with the back end at all, you can simply kick off the back end process via the server dev script located in the root of this directory. It will handle decrypting secrets, starting the server, and reencrypting secrets once you shut the server down. *If you do intend to work with the back end, skip this step and see the below debug instructions*

```shell
./hmiServerDev.sh start
```

Finally, to start the client, run the command in the root directory

```shell
yarn dev
```

<details>
<summary><b>Debugging the Client in IntelliJ</b></summary>

Create a new IntelliJ run configuration with the following settings:
* Type: JavaScript Debug
* Name: `Terarium Client` (or whatever you want)
* URL: `http://localhost:8080`
* Browser: `Chrome` (or whatever you want)
* Check "Ensure breakpoints are detected when loading scripts"

Save your configuration, and choose Debug from the Run menu. You will now hit breakpoints set in your front end code. Note that prior to running this config you'll need to have run `yarn dev` separately

  ![debug Front End](docs/debugFrontEnd.png)

</details>
<details>
<summary><b>Debugging the Server in IntelliJ</b></summary>
The easiest way to debug the back end is to use the auto-created debug profile in IntelliJ. However first you'll have to
create a new run config to decrypt the application secrets and then modify the default run profile to include it.

1) Create a new run profile named "start-server-ide" which runs the `./hmiServerDev start-server-ide` command:
   ![start-server-ide.png](docs%2Fstart-server-ide.png)
2) Navigate now to the default created Spring Boot run profile. If you don't have one, create one and set the properties to what you see below.
   * Add a "Before Launch > Add before launch task" option 
![springboot-config-add-run-options.png](docs%2Fspringboot-config-add-run-options.png) 
   * Select "Run Another Configuration" and select the `start-server-ide` run config you just created. **Slot it first.**
   * In the _Active profiles_ field, enter `default,secrets`
![springboot-config-active-profiles.png](docs%2Fspringboot-config-active-profiles.png)
</details>


## Testing

Start local dev server for Vue webapp, with Hot Module Replacement.

## Debugging REST Api
### Swagger
For convenience, a [Swagger](https://swagger.io/) UI is provided to experiment with the API. With the server running
locally (eg, not via Docker), it can be accessed at [http://localhost:3000/swagger-ui/index.html](http://localhost:3000/swagger-ui/index.html).
To authorize requests, click the `Authorize` button and click `Authorize` on the modal that appears. You can enter the credentials
of the user you want to use to make requests.
Note: In order to "logout" from Swagger, you will need to clear your browser's cookies.
### Postman
A Postman collection can be imported via the OpenAPI specification at [http://localhost:3000/v3/api-docs](http://localhost:3000/v3/api-docs).
In Postman:
1. Click the `Import` button at the top left of the Postman window
2. Paste in the the URL above and click `Continue`
3. Click `Import` and you should have a new collection named `Pantera APIs`
4. Click on the collection and click on the `Authorization` tab
5. Ensure the `Client ID` is `app` and the `Authorize using browser` checkbox is checked


```
yarn dev
# equivalent to: yarn workspace hmi-client run dev
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

## Services and Ports

* HMI Server - 3000
* Model Service - 3010
* Data Service - 3020

## Packaging and running the client

## Working with Docker

```
# Docker build
docker build . -t <image_name>

# Run, make Terarium available on http://localhost:3000
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
