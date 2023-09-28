
SERVER_DIR="packages/server"
ENCRYPTED_FILE="src/main/resources/application-secrets.properties.encrypted"
DECRYPTED_FILE="src/main/resources/application-secrets.properties"
VAULT_PASSWORD=""~/askem-vault-id.txt""


case ${1} in
	-h | --help)
		COMMAND="help"
		;;
	start)
		COMMAND="start"
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
		cd ${SERVER_DIR} || exit
		echo "Decrypting local secrets vault"
		ansible-vault decrypt --vault-password-file ${VAULT_PASSWORD} --output ${DECRYPTED_FILE} ${ENCRYPTED_FILE}
    gradle bootRun --args='--spring.profiles.active=default,secrets'
    echo "Deleting local secrets fault"
    rm ${DECRYPTED_FILE}
    ;;
  decrypt)
  	cd ${SERVER_DIR} || exit
  	ansible-vault decrypt --vault-password-file ${VAULT_PASSWORD} --output ${DECRYPTED_FILE} ${ENCRYPTED_FILE}
  	;;
 	encrypt)
 		cd ${SERVER_DIR} || exit
   	ansible-vault encrypt --vault-password-file ${VAULT_PASSWORD} --output ${ENCRYPTED_FILE} ${DECRYPTED_FILE}
   	;;
  help)
  	echo "
    	Usage:
    			${0} start              Decrypts the secrets file, starts the application via 'gradle bootRun', then removes secrets after run
    			${0} decrypt            Decrypt secrets (${ENCRYPTED_FILE}) to an unencrypted file (${DECRYPTED_FILE})
    			${0} encrypt            Encrypts ${DECRYPTED_FILE} to the checked in secrets file, ${ENCRYPTED_FILE}"
    ;;
esac
