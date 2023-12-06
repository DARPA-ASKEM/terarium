#!/usr/bin/env bash

# Description: This script is used to start the server locally for development purposes.
#              It will decrypt the secrets file, start the server, then remove the secrets file.

SERVER_DIR="packages/server"
SECRET_FILES=()
SECRET_FILES+=("${SERVER_DIR}/src/main/resources/application-secrets.properties")
SECRET_FILES+=("packages/db-migration/src/main/resources/application-secrets.properties")
SECRET_FILES+=("containers/secrets.env")
VAULT_PASSWORD=""~/askem-vault-id.txt""

function decrypt_secrets() {
  echo "Decrypting local secrets vault"
  for SECRET_FILE in "${SECRET_FILES[@]}"; do
    echo "decrypting file: ${SECRET_FILE}"
    ansible-vault decrypt --vault-password-file ${VAULT_PASSWORD} --output ${SECRET_FILE} ${SECRET_FILE}.encrypted
  done
}

function encrypt_secrets() {
  echo "Encrypting local secrets vault"
  for SECRET_FILE in "${SECRET_FILES[@]}"; do
    echo "encrypting file: ${SECRET_FILE}"
    ansible-vault encrypt --vault-password-file ${VAULT_PASSWORD} --output ${SECRET_FILE}.encrypted ${SECRET_FILE}
  done
}

function delete_secrets() {
  echo "Deleting local secrets vault"
  for SECRET_FILE in "${SECRET_FILES[@]}"; do
    echo "deleting file: ${SECRET_FILE}"
    rm ${SECRET_FILE}
  done
}

function deploy_remote() {
  echo "Deploying containers for development against staging services"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-remote.yml pull
  docker compose --env-file containers/.env --file containers/docker-compose-remote.yml up --detach --wait
}

function deploy_local() {
  echo "Deploying containers for development against local services"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-local.yml pull
  docker compose --env-file containers/.env --file containers/docker-compose-local.yml up --detach --wait
}

function deploy_full() {
  echo "Locally run all containers"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-full.yml pull
  docker compose --env-file containers/.env --file containers/docker-compose-full.yml up --detach --wait
}

function stop_remote() {
  echo "stopping local containers used for remote dev"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-remote.yml down
}

function stop_local() {
  echo "Stopping local dev containers"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-local.yml down
}

function stop_full() {
  echo "Stopping all containers"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-full.yml down
}

function start_remote() {
  echo "Starting remote server"
  cd ${SERVER_DIR} || exit
  ./gradlew bootRun --args='--spring.profiles.active=default,secrets'
  cd - || exit
}

function start_local() {
  echo "Starting local server"
  cd ${SERVER_DIR} || exit
  ./gradlew bootRun --args='--spring.profiles.active=default,secrets,local'
  cd - || exit
}

function build_docker_compose() {
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-full.yml $(for customfile in `ls docker-compose.custom*.y*ml 2> /dev/null`; do echo -n " -f $customfile"; done) config > ./docker-compose.yml
}

while [[ $# -gt 0 ]]; do
  case ${1} in
  -h | --help)
    COMMAND="help"
    ;;
  start)
    COMMAND="start"
    ENVIRONMENT="$2"
    SERVER="$3"
    shift
    shift
    ;;
  stop)
    COMMAND="stop"
    ENVIRONMENT="$2"
    shift
    ;;
  encrypt)
    COMMAND="encrypt"
    ;;
  decrypt)
    COMMAND="decrypt"
    ;;
  docker-compose)
    COMMAND="docker-compose"
    ;;
  *)
    echo "hmiServerDev.sh: illegal option"
    break
    ;;
  esac
  shift
done

# Default COMMAND to start if empty
COMMAND=${COMMAND:-"help"}
ENVIRONMENT=${ENVIRONMENT:-"remote"}
SERVER=${SERVER:-"false"}

echo "COMMAND: $COMMAND"
echo "ENVIRONMENT: $ENVIRONMENT"
echo "SERVER: $SERVER"

case ${COMMAND} in
  start)
    decrypt_secrets
    case ${ENVIRONMENT} in
      remote)
        deploy_remote
        ;;
      local)
        deploy_local
        ;;
      full)
        deploy_full
        ;;
      *)
        echo "Illegal ENVIRONMENT"
        break
        ;;
    esac
    if [ ${SERVER} == "run" ]; then
      if [ ${ENVIRONMENT} == "remote" ]; then
        start_remote
      elif [ ${ENVIRONMENT} == "local" ]; then
        start_local
      fi
      delete_secrets
    fi
    ;;
  stop)
    decrypt_secrets
    case ${ENVIRONMENT} in
      remote)
        stop_remote
        ;;
      local)
        stop_local
        ;;
      full)
        stop_full
        ;;
      *)
        echo "Illegal ENVIRONMENT"
        break
        ;;
    esac
    delete_secrets
     ;;
  decrypt)
    decrypt_secrets
    ;;
  encrypt)
    encrypt_secrets
     ;;
  docker-compose)
    decrypt_secrets
    build_docker_compose
    delete_secrets
    ;;
  help)
    echo "
      DESCRIPTION:
        Terarium Development scripts

      SYNOPSIS:
        hmiServerDev.sh [start ENVIRONMENT [run] (optional) | encrypt | decrypt]

      start
        ENVIRONMENT
          remote | local | full (default: remote)  Indicate which environment to develop against

        run (default: null) Indicate whether to run the server after starting the containers

      stop
        ENVIRONMENT
          remote | local | full (default: remote)  Indicate which containers to stop

      OTHER COMMANDS:
        encrypt
          Encrypts the secrets file
        decrypt
          Decrypts the secrets file
        docker-compose
          Generates a combined single docker-compose.yml file for local development
        help
          Displays this help message
      "
    ;;
esac
