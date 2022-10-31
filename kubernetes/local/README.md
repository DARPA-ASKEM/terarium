
# Authorization Gateway

## Background

See [Uncharted-Auth](https://github.com/unchartedsoftware/uncharted-auth) for details about building the Gateway containers.

## Requirements

### Enabling Kubernetes

You will need a Kubernetes node running locally, the simplist for development is probably Docker Desktop's Kubernetes capability as it give the easiest access to your local machine from within the Kubernetes (or Docker) containers, but there are other options if one wishes.

To enable Docker Desktop Kubernetes bring up the Docker Desktop `Dashboard`, goto `Settings` (the gear icon), select `Kubernetes`, and `Enable Kubernetes`.  This will require a bried installation and restart of Docker, but once completed you can test that all is working by issuing:

`kubectl get pods` and should see something like

```
NAME                                READY   STATUS    RESTARTS   AGE
```

### Pulling required containers

TODO: talk about `kubectl create secret docker-registry <name> ...`

TODO: edit deployment files to use docker-registry

### Checking errors launching containers

TODO: talk about `kubectl describe <pod-name>`

## Launching the Gateway

To launch the Gateway run `./gateway.sh up`.  This will stand up the Gateway services which are comprised of an Apache HTTPD server, a Keycloak server, and a Postgres server for Keycloak.

Running `./gateway.sh status` will show the status of the various Gateway services and if there are problems with a *pod* (it says something like CrashLoopBackoff or similar), then running `kubectl logs -f <full-pod-name>` will show the log of the problematic container.  The *full-pod-name* is shown in the list of pods when retrieving the status.

To bring the Gateway stack down, simply run `./gateway.sh down` and all the services will be torn down.

### Private Registries
If the deployments access private registries, Kubernetes will not be able to automatically pull those images for you. To do so, a secret with the required credentials is required and used for the deployment. To do this refer to the [registry documentation](../../.github/CONTRIBUTING.md#kubernetes).

## Theming Keycloak
It is possible to theme different Keycloak views. To do so the new themes should be placed under `/opt/keycloak/themes/` directory. This local deployment shows and example of how to retheme the login page by loading a data image that contains the theme that is copied over to the Keycloak POD on initialization. If additional themes are made for other views the main theme image should be update as described [here](../../keycloak-theme/README.md).
