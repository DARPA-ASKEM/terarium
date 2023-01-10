# Document-Service Project

The document-service is a stand alone layer between the [HMI-Server](../hmi-server/README.md) and external document
services like xDD.

This project uses Quarkus, the Supersonic Subatomic Java Framework.
If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application

From the orchestration project, the standard `./deploy-terarium.sh up` command will also start this layer up

## Running the application in dev mode

When terarium is run in dev mode, this server will also have to be manually started (similar to the HMI-Server). You can
do that by running one of the following commands from the root of this project:

```shell script
./gradlew quarkusDev
```

OR

```shell script
quarkus dev
```

### A note on development...

The API in this project cannot be changed without changing the corresponding calls from the HMI-Server project for
document access.
