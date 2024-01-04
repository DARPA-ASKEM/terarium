####################
###   CONSTANTS
####################
PUBLIC_GROUP_NAME="Public"
ASKEM_ADMINS_GROUP_NAME="ASKEM Admins"

PROJECT_1_ID="7cbc9b08-34ea-4641-9ffd-0417bd298bf1"
PROJECT_2_ID="530b11c6-7879-4267-bc44-67f0071f4f43"

ROLE_USER="user"
ROLE_ADMIN="admin"

RELATIONSHIP_MEMBER="member"
RELATIONSHIP_ADMIN="admin"
RELATIONSHIP_READER="reader"

####################
# Check Env Vars
####################

if [ -z ${TERARIUM_KEYCLOAK_ADMIN_CLIENT_ID} ]; then echo "ERROR: environment variable \"TERARIUM_KEYCLOAK_ADMIN_CLIENT_ID\" not set"; exit 1; fi
if [ -z ${TERARIUM_KEYCLOAK_ADMIN_USERNAME} ]; then echo "ERROR: environment variable \"TERARIUM_KEYCLOAK_ADMIN_USERNAME\" not set"; exit 1; fi
if [ -z ${TERARIUM_KEYCLOAK_ADMIN_PASSWORD} ]; then echo "ERROR: environment variable \"TERARIUM_KEYCLOAK_ADMIN_PASSWORD\" not set"; exit 1; fi
if [ -z ${TERARIUM_KEYCLOAK_URL} ]; then echo "ERROR: environment variable \"TERARIUM_KEYCLOAK_URL\" not set"; exit 1; fi
if [ -z ${TERARIUM_KEYCLOAK_REALM} ]; then echo "ERROR: environment variable \"TERARIUM_KEYCLOAK_REALM\" not set"; exit 1; fi
if [ -z ${SPICEDB_TARGET} ]; then echo "ERROR: environment variable \"SPICEDB_TARGET\" not set"; exit 1; fi
if [ -z ${SPICEDB_SHARED_KEY} ]; then echo "ERROR: environment variable \"SPICEDB_SHARED_KEY\" not set"; exit 1; fi

# Params:
#   GROUP_NAME - name of group to get/create
# Returns
#   GROUP_ID - id of group
function getGroupIdAndCreateIfMissing() {
  local GROUP_NAME=$1
  local GROUP_ID=$(echo "${GROUPS_JSON}" | jq -r ".[] | select(.name | contains(\"${GROUP_NAME}\")) | .id")
  if [ -z ${GROUP_ID} ]; then
    echo "INFO: creating group \"${GROUP_NAME}\""
    curl -X POST -d "{ \"name\": \"${GROUP_NAME}\"}" -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -H "Accept: application/json" ${TERARIUM_KEYCLOAK_URL}/admin/realms/${TERARIUM_KEYCLOAK_REALM}/groups 2>/dev/null
    GROUPS_JSON=$(curl -H "Authorization: Bearer $ACCESS_TOKEN" -H "Accept: application/json" ${TERARIUM_KEYCLOAK_URL}/admin/realms/${TERARIUM_KEYCLOAK_REALM}/groups 2>/dev/null)
    GROUP_ID=$(echo "${GROUPS_JSON}" | jq -r ".[] | select(.name | contains(\"${GROUP_NAME}\")) | .id")
    if [ -z ${GROUP_ID} ]; then
      echo "ERROR: could not find group \"${GROUP_NAME}\"'s id"
      exit 1
    fi
  fi

  echo ${GROUP_ID}
}

# Params:
#   ROLE - name of role to get users from
#   GROUP_ID - id of group to add user to
#   RELATIONSHIP - group relationship to set for user
function addUsersForRoleToGroup() {
  local ROLE=$1
  local GROUP_ID=$2
  local RELATIONSHIP=$3

  local USER_IDS=$(curl -H "Authorization: Bearer $ACCESS_TOKEN" -H "Accept: application/json" ${TERARIUM_KEYCLOAK_URL}/admin/realms/${TERARIUM_KEYCLOAK_REALM}/roles/${ROLE}/users 2>/dev/null | jq -r ".[] | .id")
  for USER_ID in ${USER_IDS}; do
    local USER_FOUND=$(zed ${SPICEDB_SETTINGS} relationship read group:${GROUP_ID} 2>/dev/null | grep "${RELATIONSHIP} user:${USER_ID}")
    if [ -z "${USER_FOUND}" ]; then
      echo "INFO: creating Group ${GROUP_ID} relationship ${RELATIONSHIP} with User ${USER_ID}"
      zed ${SPICEDB_SETTINGS} relationship create group:${GROUP_ID} ${RELATIONSHIP} user:${USER_ID} 2>/dev/null
    else
      echo "INFO: USER \"${USER_ID}\" is a ${RELATIONSHIP} of Group \"${GROUP_ID}\""
    fi
  done
}

# Params:
#   PROJECT_ID - id of project to add group to
#   GROUP_ID - id of group
#   GROUP_NAME - for logging
#   RELATIONSHIP - group relationship to set for user
function assignGroupToProject() {
  local PROJECT_ID=$1
  local GROUP_ID=$2
  local RELATIONSHIP=$3

  local GROUP_FOUND=$(zed ${SPICEDB_SETTINGS} relationship read project:${PROJECT_ID} 2>/dev/null | grep "${RELATIONSHIP} group:${GROUP_ID}")
  if [ -z "${GROUP_FOUND}" ]; then
    echo "INFO: creating Project ${PROJECT_ID} relationship with Group ${GROUP_ID}"
    zed ${SPICEDB_SETTINGS} relationship create project:${PROJECT_ID} ${RELATIONSHIP} group:${GROUP_ID} 2>/dev/null
  else
    echo "INFO: Group \"${GROUP_ID}\" is a ${RELATIONSHIP} of Project \"${PROJECT_ID}\""
  fi
}

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

PUBLIC_GROUP_ID=$(getGroupIdAndCreateIfMissing ${PUBLIC_GROUP_NAME})
ASKEM_ADMIN_GROUP_ID=$(getGroupIdAndCreateIfMissing ${ASKEM_ADMINS_GROUP_NAME})

echo "INFO: Group \"${ASKEM_ADMINS_GROUP_NAME}\" has id \"${ASKEM_ADMIN_GROUP_ID}\""
echo "INFO: Group \"${PUBLIC_GROUP_NAME}\" has id \"${PUBLIC_GROUP_ID}\""

# add users to Public Group
addUsersForRoleToGroup ${ROLE_USER} ${PUBLIC_GROUP_ID} ${RELATIONSHIP_MEMBER}
# add admin users to Admin Group
addUsersForRoleToGroup ${ROLE_ADMIN} ${ASKEM_ADMIN_GROUP_ID} ${RELATIONSHIP_ADMIN}

# assign public readership to project 1
assignGroupToProject ${PROJECT_1_ID} ${PUBLIC_GROUP_ID} ${RELATIONSHIP_READER}
# assign public readership to project 2
assignGroupToProject ${PROJECT_2_ID} ${PUBLIC_GROUP_ID} ${RELATIONSHIP_READER}

