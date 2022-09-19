To launch the Gateway run `./gateway.sh up`.  This will stand up the Gateway services which are comprised of an Apache HTTPD server, a Keycloak server, and a Postgres server for Keycloak.

Running `./gateway.sh status` will show the status of the various Gateway services and if there are problems with a *pod* (it says something like CrashLoopBackoff or similar), then running `kubectl logs -f <full-pod-name>` will show the log of the problematic container.  The *full-pod-name* is shown in the list of pods when retrieving the status.

To bring the Gateway stack down, simply run `./gateway.sh down` and all the services will be torn down.