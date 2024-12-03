#!/usr/bin/env bash

# Description: This script is used to start the server locally for development purposes.
#              It will decrypt the secrets file, start the server, then remove the secrets file.

SERVER_DIR="packages/server"
SECRET_FILES=()
SECRET_FILES+=("${SERVER_DIR}/src/main/resources/application-secrets.properties")
SECRET_FILES+=("containers/secrets.env")
VAULT_PASSWORD=""~/askem-vault-id.txt""
export COMPOSE_PROJECT_NAME="terarium"

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

function deploy_local_lean() {
  echo "Deploying containers for development against local services"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-local-lean.yml up --detach --wait
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

function stop_local_lean() {
  echo "Stopping local dev containers"
  cat containers/common.env containers/secrets.env > containers/.env
  docker compose --env-file containers/.env --file containers/docker-compose-local-lean.yml down
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
ENVIRONMENT=${ENVIRONMENT:-"local"}
SERVER=${SERVER:-"false"}

# We keep staging as a valid environment for legacy purposes
VALID_ENVIRONMENTS=("local" "staging" "full" "ll")
ENVIRONMENT_IS_VALID=0
for env in ${VALID_ENVIRONMENTS[@]}; do
  echo "checking $ENVIRONMENT against $env"
  if [ "${env}" = "${ENVIRONMENT}" ]; then
    echo "setting ENVIRONMENT_IS_VALID to 1"
    ENVIRONMENT_IS_VALID=1
  fi
done
echo "value of ENVIRONMENT_IS_VALID is ${ENVIRONMENT_IS_VALID}"
if [ ${ENVIRONMENT_IS_VALID} -eq 0 ]; then
  echo "Illegal ENVIRONMENT \"${ENVIRONMENT}\""
  COMMAND="help"
else
  echo "COMMAND: $COMMAND"
  echo "ENVIRONMENT: $ENVIRONMENT"
  echo "SERVER: $SERVER"
fi

case ${COMMAND} in
  start)
    decrypt_secrets
    case ${ENVIRONMENT} in
      local)
        deploy_local
        ;;
      staging)
        deploy_local
        ;;
      full)
        deploy_full
        ;;
      ll)
        deploy_local_lean
        ;;
    esac
    if [ ${SERVER} == "run" ]; then
      if [ ${ENVIRONMENT} == "local" ]; then
        start_local
      elif [ ${ENVIRONMENT} == "staging" ]; then
        start_local
      fi
      delete_secrets
    fi
    ;;
  stop)
    decrypt_secrets
    case ${ENVIRONMENT} in
      local)
        stop_local
        ;;
      staging)
        stop_local
        ;;
      full)
        stop_full
        ;;
      ll)
        stop_local_lean
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
          local | full | ll (default: local)  Indicate which environment to develop against
              (ll: local_lean to run local with the absolute minimal support to run hmiServer for development)

        run (default: null) Indicate whether to run the server after starting the containers

      stop
        ENVIRONMENT
          local | full | ll (default: local)  Indicate which containers to stop

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
