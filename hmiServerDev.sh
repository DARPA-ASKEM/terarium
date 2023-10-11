#!/usr/bin/env bash

# Description: This script is used to start the server locally for development purposes.
#              It will decrypt the secrets file, start the server, then remove the secrets file.

SERVER_DIR="packages/server"
ENCRYPTED_FILE="${SERVER_DIR}/src/main/resources/application-secrets.properties.encrypted"
DECRYPTED_FILE="${SERVER_DIR}/src/main/resources/application-secrets.properties"
VAULT_PASSWORD=""~/askem-vault-id.txt""

function decrypt_secrets() {
	echo "Decrypting local secrets vault"
	ansible-vault decrypt --vault-password-file ${VAULT_PASSWORD} --output ${DECRYPTED_FILE} ${ENCRYPTED_FILE}
}

function encrypt_secrets() {
	echo "Encrypting local secrets vault"
	ansible-vault encrypt --vault-password-file ${VAULT_PASSWORD} --output ${ENCRYPTED_FILE} ${DECRYPTED_FILE}
}

function delete_secrets() {
	echo "Deleting local secrets vault"
	rm ${DECRYPTED_FILE}
}

function deploy_containers() {
	echo "Deploying local containers"
	docker compose --file containers/docker-compose-base.yml --project-name terarium up --detach --no-recreate --wait
}

function start_server() {
	echo "Starting local server"
	cd ${SERVER_DIR} || exit
	./gradlew bootRun --args='--spring.profiles.active=default,secrets'
	cd - || exit
}

case ${1} in
	-h | --help)
		COMMAND="help"
		;;
	start)
		COMMAND="start"
		;;
	start-server-ide)
  	COMMAND="start-server-ide"
  	;;
	decrypt)
		COMMAND="decrypt"
		;;
	encrypt)
		COMMAND="encrypt"
		;;
esac

# Default COMMAND to start if empty
COMMAND=${COMMAND:-"start"}

case ${COMMAND} in
	start)
		decrypt_secrets
    deploy_containers
    start_server
    delete_secrets
    ;;
	start-server-ide)
  	decrypt_secrets
  	deploy_containers
  	;;
  decrypt)
  	decrypt_secrets
  	;;
 	encrypt)
 		encrypt_secrets
   	;;
  help)
  	echo "
    	Usage:
    			${0} start              		Decrypts the secrets file, starts the application via 'gradle bootRun', then removes secrets after run
    			${0} start-server-ide  			Decrypts the secrets file, starts the application via IntelliJ, then removes secrets after run
    			${0} decrypt            		Decrypt secrets (${ENCRYPTED_FILE}) to an unencrypted file (${DECRYPTED_FILE})
    			${0} encrypt            		Encrypts ${DECRYPTED_FILE} to the checked in secrets file, ${ENCRYPTED_FILE}"
    ;;
esac
