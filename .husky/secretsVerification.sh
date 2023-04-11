
echo "Running secrets encryption check"
# Place files that need to be encrypted with ansible-vault here, path relative to parent Terarium project.
ENCRYPTED_FILES=(
"packages/services/hmi-server/src/main/resources/application-secrets.properties.encrypted"
)

for file in "${ENCRYPTED_FILES[@]}"; do
	if ! cat "$file" | grep --quiet "^\$ANSIBLE_VAULT"; then
		echo "$file is unencrypted"
		exit 1
	fi
done

exit 0


