
# Terarium

Terarium is the client application for the ASKEM program providing capabilities to create, modify, simulate, and publish
machine extracted models.

[app.terarium.ai](https://app.terarium.ai/)

## Table of Contents

1. [Project Status](#project-status)
1. [Getting Started](#getting-started)
  1. [Dependencies](#dependencies)
  1. [Developing and Debugging](#running-and-debugging)
     1. [Running the Client](#running-the-client)
     1. [Running the Server](#running-the-server)
     1. [Running only the Data Services](#running-only-the-data-services)
  1. [Testing](#testing)
     1. [Running the Client Tests](#running-the-client-tests)
     1. [Running the Server Tests](#running-the-server-tests)
1. [Contributing](#conventional-commits)
1. [Conventional Commits](#conventional-commits)
1. [License](#license)

## Project Status
[![Build and Publish](https://github.com/DARPA-ASKEM/Terarium/actions/workflows/publish.yaml/badge.svg?event=push)](https://github.com/DARPA-ASKEM/TERArium/actions/workflows/publish.yaml)

[![Client E2E Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client-e2e.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client-e2e.yaml)
[![Client Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-client.yaml)
[![Server Tests](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-server.yaml/badge.svg)](https://github.com/DARPA-ASKEM/terarium/actions/workflows/test-server.yaml)

## Getting Started

### Dependencies

The Terarium client is built with Typescript and Vue3. The Terarium server is built with Java and Spring Boot. To run and
develop Terarium, you will need these as a prerequisite:

- [Yarn 4](https://yarnpkg.com/getting-started/install)
- [NodeJS 20](https://nodejs.org/en/download/current/)
- [JDK 17](https://adoptium.net/temurin)
- [Gradle 8](https://gradle.org/install/)
- [Ansible](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html)

There are many ways/package managers to install these dependencies. We recommend using [Homebrew](https://brew.sh/) on MacOS.

```bash
brew install openjdk@17
brew install gradle
brew install node
brew install yarn
brew install ansible
```

<details>
<summary><b>Windows Subsystem Linux Setup</b></summary>
Instructions for users on a Windows System

### Install WSL 
Download from Windows Store App / [Website](https://apps.microsoft.com/home?hl=en-US&gl=US) a WSL developer tools app version you prefer to work with.

In the Windows Command Prompt Terminal enter this to change the sudo password for WSL user.
```shell
wsl --user root
passwd <username>
```

### Install Dependencies

In the WSL Terminal input these commands to install the dependencies (Ubuntu install commands).
```shell
sudo apt-get upgrade
sudo apt-get install build-essential
sudo apt install openjdk-17-jre-headless
sudo apt install gradle
sudo apt install python3-pip
python3 -m pip install --user ansible
```
</details>

### Ansible Vault Password File

You will need to have the ansible askem vault password in your home directory in a file named `askem-vault-id.txt`. You can find this file in the ASKEM TERArium (Shared External) drive on Google Drive. **This file is not included in the repository for security reasons. Please contact the team for access to this file.**

## Running and Debugging
There is a companion project to Terarium which handles spinning up the required external services. Depending on what you're doing this can be configured to run all or some of the related services. If this is necessary, you will need to start the orchestration project up before continuing (see documentation [here](https://github.com/DARPA-ASKEM/orchestration)).

### Running the Client
To install client package dependencies, run the following command in the root directory:

```shell
yarn install
```

Running the client in dev mode requires running two processes: the local client dev server and the [typescript model generation](docs/Typescript_Model_Generation.md).

To run both processes with a single command:

```shell
yarn dev
```

To run as individual processes:

```shell
yarn workspace hmi-client run dev                       # client development server
yarn workspace @uncharted/server-type-generator run dev # typescript model generator
```

To generate the typescript models as a single command:

```shell
yarn workspace @uncharted/server-type-generator run generateTypes
```

The client, when running with the command `yarn dev`, connects to the server in the dev environment, enabling client-side development without the need to spin up the server locally.

To run the client while connecting to the server running locally, use the following command:

```shell
yarn local
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
<summary><b>Debugging the Client in VSCode</b></summary>

Ensure the following configuration is in the `.vscode/launch.json` directory:

```json
{
  "type": "node",
  "request": "launch",
  "name": "HMI Client",
  "runtimeExecutable": "yarn",
  "cwd": "${workspaceFolder}/packages/client",
  "runtimeArgs": ["dev"],
  // Run vite dev server using `yarn dev` and then launch the chrome browser
  "serverReadyAction": {
    "action": "debugWithChrome",
    "pattern": "Local:[ ]+http://.+:([0-9]+)",
    "uriFormat": "http://localhost:%s",
    "webRoot": "${workspaceFolder}/packages/client"
  }
}
```
</details>

### Running the Server

If you don't intend to run the backend with a debugger, you can simply kick off the back end process via the `hmiServerDev.sh` script located in the root of this directory. It will handle decrypting secrets, starting the server, and re-encrypting secrets once you shut the server down. *If you do intend to debug the back end, skip this step and see the below debug instructions*

```shell
./hmiServerDev.sh start local run
```

> Note: to run everything local you need to update your `/etc/hosts` with the following `127.0.0.1 minio`.
> ```shell
> sudo sh -c 'grep -qF "127.0.0.1  minio" /etc/hosts || echo "127.0.0.1  minio" >> /etc/hosts'
>```

If you are going to run the server using the Intellij / VSCode debugger, you can run just the required containers and handle decryption with the following command
```shell
./hmiServerDev.sh start local
```

If you're looking to just decrypt or encrypt secrets you can run:

```shell
./hmiServerDev.sh decrypt
```
or
```shell
./hmiServerDev.sh encrypt
```

If running decrypt, you'll see the contents of `application-secrets.properties.encrypted` decrypted to plain text.
There should now be a `application-secrets.properties` file in the `packages/server/src/main/resources` dir.
If running encrypt, `application-secrets.properties`'s content will be encrypted into the *.encrypted file.

<details>
<summary><b>Debugging the Server in IntelliJ</b></summary>

1) Create a new run Spring Boot Run/Debug configuration adding the `default`, `secrets`, and `local` profiles:
   ![server-intellij-config.png](docs/server-intellij-config.png)
</details>

<details>
<summary><b>Debugging the Server in VSCode</b></summary>

1) Ensure you have the `Extension Pack for Java` extension pack installed.
2) If you have the `Gradle for Java` extension installed, disable the gradle build server: [it causes problems with building and live reload](https://github.com/redhat-developer/vscode-java/issues/2338).
3) Ensure the following configuration is in the `.vscode/launch.json` directory:

```json
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "TerariumApplication",
            "request": "launch",
            "mainClass": "software.uncharted.terarium.hmiserver.TerariumApplication",
            "projectName": "server",
            "args": [
              "--spring.profiles.active=default,secrets,local"
            ]
        }
    ]
}
```
</details>

### Debugging task runners

To debug task runners locally, follow these steps to modify specific Docker Compose and configuration files:

1. **Update `docker-compose-local.yml`:**
  - Open `docker-compose-local.yml`.
  - Uncomment the line that includes `scripts/docker-compose-taskrunner.yml`.

2. **Configure `scripts/docker-compose-taskrunner.yml`:**
  - Open `scripts/docker-compose-taskrunner.yml`.
  - Uncomment the service definition for the specific task runner you want to run locally.

3. **Edit `application-local.properties`:**
  - Open `application-local.properties`.
  - In the **TaskRunner Configuration** section, comment out the `addresses`, `username`, and `password` properties for the task runner you want to run locally.

Following these steps will enable you to debug the task runners in a local environment.

### Running only the Data Services

A functional `docker-compose-lean.yml` with all services necessary to run the `terarium` backend can be spun up with the following:

```shell
docker compose --file containers/docker-compose-lean.yml pull
docker compose --file containers/docker-compose-lean.yml up --detach --wait
```

This will standup a local `terarium` server on port `3000` supporting all data service endpoints.

The `terarium` backend uses `OAuth2.0` via `keycloak` for user authentication. In order to make calls to the data services simpler, a `service-user` can be used by providing a basic auth credential instead.

Please use the following basic auth credential if running `docker-compose-lean.yml`:

```
'Authorization: Basic YWRhbTphc2RmMUFTREY='
```

If you prefer the JSON request / response keys to be `snake_case` rather than `camelCase` include the following header in any data service request:

```
'X-Enable-Snake-Case'
```

If integrating the `docker-compose-lean.yml` into another repo, the following files and directory structure is expected:

```
- scripts
  - init.sql                     // initialize the postgres databases
  - realm
    - Terarium-realm.json        // keycloak realm definition
    - Terarium-users-0.json      // keycloak user definitions
- docker-compose-lean.yml
```

## Testing

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
3. Click `Import` and you should have a new collection named `Terarium APIs`
4. Click on the collection and click on the `Authorization` tab
5. Ensure the `Client ID` is `app` and the `Authorize using browser` checkbox is checked

### Running the Client Tests

```shell
yarn workspace hmi-client run test
```

### Running the Server Tests

```shell
./gradlew test
```

To run a specific test, or a set of tests, you can specify a `--tests=PATTERN` filter. Note however, for this option to work, you need to be under `terarium/packages/server` directory when running gradlew.

```shell
./gradlew test --rerun-tasks --tests="WorkflowS*"
```


## Contributing

Please see further documentation in the [Terarium Contributing Guide](.github/CONTRIBUTING.md)

## Conventional Commits

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
