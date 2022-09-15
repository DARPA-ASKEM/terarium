#!/bin/bash

kubectl apply -f gateway-postgres-service.yaml -f gateway-postgres-deployment.yaml -f gateway-keycloak-realm.yaml -f gateway-keycloak-service.yaml -f gateway-keycloak-deployment.yaml -f gateway-httpd-config.yaml -f gateway-httpd-htdocs.yaml -f gateway-httpd-service.yaml -f gateway-httpd-deployment.yaml

kubectl get po,svc,configMap
