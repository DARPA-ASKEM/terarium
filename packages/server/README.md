# Terarium Server

This is the main Terarium Server application.

## Docker Images

There are two versions of the docker images. One is a regular JVM build and the other is an optimized native build. Each can be built individually with the following commands:

> NOTE: BuildX should be enabled (if using latest Docker Desktop with enging >=v20 this should be automatically enabled for you)

> Docker files are set to be run with the root context

### Building JVM Image

To build the JVM image simply run the following from the root. Alternatively change the location of the Dockerfile if running from within the server directory
```sh
docker buildx build -f modules/server/docker/Dockerfile -t docker.uncharted.software/terarium:server .
```

### Building Native Image
To build the NATIVE image simply run the following from the root directory.
```sh
docker buildx build -f modules/server/docker/Dockerfile.native -t docker.uncharted.software/terarium:server .
```
