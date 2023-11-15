####################
###   CONSTANTS
####################
PUBLIC_GROUP_NAME="Public"
ASKEM_ADMINS_GROUP_NAME="ASKEM Admins"

PROJECT_1_ID="1"
PROJECT_2_ID="2"


####################
# Check Env Vars
####################

if [ -z ${TERARIUM_KEYCLOAK_ADMIN_CLIENT_ID} ]; then exit 2; fi
if [ -z ${TERARIUM_KEYCLOAK_ADMIN_USERNAME} ]; then exit 3; fi
if [ -z ${TERARIUM_KEYCLOAK_ADMIN_PASSWORD} ]; then exit 4; fi
if [ -z ${TERARIUM_KEYCLOAK_URL} ]; then exit 5; fi
if [ -z ${TERARIUM_KEYCLOAK_REALM} ]; then exit 6; fi
if [ -z ${SPICEDB_TARGET} ]; then exit 7; fi
if [ -z ${SPICEDB_SHARED_KEY} ]; then exit 8; fi

# Configure spicedb

SPICEDB_SETTINGS="--endpoint ${SPICEDB_TARGET} --token ${SPICEDB_SHARED_KEY}"
if [ "${SPICEDB_INSECURE}" = "true" ]; then
  SPICEDB_SETTINGS="${SPICEDB_SETTINGS} --insecure"
fi

# Create SpiceDB schema
zed ${SPICEDB_SETTINGS} schema write schema 2>/dev/null

# Aquire Access Token for Keycloak
ACCESS_TOKEN=$(curl -d "client_id=${TERARIUM_KEYCLOAK_ADMIN_CLIENT_ID}" -d "username=${TERARIUM_KEYCLOAK_ADMIN_USERNAME}" -d "password=${TERARIUM_KEYCLOAK_ADMIN_PASSWORD}" -d "grant_type=password" "${TERARIUM_KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" 2>/dev/null | jq -r ".access_token")

# Get groups from Keycloak
GROUPS_JSON=$(curl -H "Authorization: Bearer $ACCESS_TOKEN" -H "Accept: application/json" ${TERARIUM_KEYCLOAK_URL}/admin/realms/${TERARIUM_KEYCLOAK_REALM}/groups 2>/dev/null)

# TODO: create missing groups
# TODO: get users
# TODO: add users to Public Group
# TODO: add admin users to Admin Group

# Get ids for groups
PUBLIC_GROUP_ID=$(echo "${GROUPS_JSON}" | jq -r ".[] | select(.name | contains(\"${PUBLIC_GROUP_NAME}\")) | .id")
ASKEM_ADMIN_GROUP_ID=$(echo "${GROUPS_JSON}" | jq -r ".[] | select(.name | contains(\"${ASKEM_ADMINS_GROUP_NAME}\")) | .id")
if [ -z ${PUBLIC_GROUP_ID} ]; then
  echo "ERROR: could not find group \"${PUBLIC_GROUP_NAME}\"'s id"
  exit 1
fi
echo "INFO: Group \"${PUBLIC_GROUP_NAME}\" has id \"${PUBLIC_GROUP_ID}\""

# assign public readership to project 1
PUBLIC_FOUND=$(zed ${SPICEDB_SETTINGS} relationship read project:${PROJECT_1_ID} 2>/dev/null | grep ${PUBLIC_GROUP_ID})
if [ -z "${PUBLIC_FOUND}" ]; then
  echo "INFO: creating Project ${PROJECT_1_ID} relationship with Group ${PUBLIC_GROUP_NAME}"
  zed ${SPICEDB_SETTINGS} relationship create project:${PROJECT_1_ID} reader group:${PUBLIC_GROUP_ID} 2>/dev/null
else
  echo "INFO: Group \"${PUBLIC_GROUP_NAME}\" is a reader of Project \"${PROJECT_1_ID}\""
fi

