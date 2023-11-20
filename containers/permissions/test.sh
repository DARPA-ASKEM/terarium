source ../.env

SPICEDB_SHARED_KEY="dev" \
SPICEDB_TARGET="localhost:50051" \
SPICEDB_INSECURE="true" \
TERARIUM_KEYCLOAK_ADMIN_CLIENT_ID="${secret_terarium_keycloak_admin_client_id}" \
TERARIUM_KEYCLOAK_ADMIN_PASSWORD="${secret_terarium_keycloak_admin_password}" \
TERARIUM_KEYCLOAK_ADMIN_USERNAME="${secret_terarium_keycloak_admin_username}" \
TERARIUM_KEYCLOAK_REALM="${secret_terarium_keycloak_realm}" \
TERARIUM_KEYCLOAK_URL="${secret_terarium_keycloak_url}" \
bash ./script.sh
