#!/usr/bin/env bash
set -e

usage() {
    echo "Usage: ${0} {trucode_id,user_identifier,device_identifier} <identifier>"
    echo "env args:"
    echo "   FORGEROCK_REALM - The realm to authenticate to. default trusona"
    echo "   FORGEROCK_URL = The url of forgerock. default: https://localhost"
}

do_api_request() {
    echo '------ SENDING REQUEST ---------'
    echo $REQUEST | jq . | sed 's/.*/>> &/'
    echo '--------------------------------'
    RESPONSE=$(curl -XPOST -H "Content-Type: application/json" -d "$REQUEST" $AUTH_URL 2>/dev/null)
    echo '------ API RESPONSE ------------'
    echo $RESPONSE | jq . | sed 's/.*/<< &/'
    echo '--------------------------------'
}

REALM="${FORGEROCK_REALM:-trusona}"
BASE_URL="${FORGEROCK_URL:-https://localhost}"
AUTH_URL="${BASE_URL}/openam/json/realms/${REALM}/authenticate"

if [ "$#" -ne 2 ]; then
    usage
    exit 1
fi

ID_TYPE=$1
IDENTIFIER=$2



REQUEST="{}"
do_api_request

# Since we are using the API directly, we can skip over the JS in the first step.
REQUEST=$RESPONSE
do_api_request

ID_TYPE_IDX=$(echo $RESPONSE | jq -r ".callbacks[1].output[1].value | index(\"${ID_TYPE}\")")

REQUEST=$(echo $RESPONSE | jq ".callbacks[1].input[0].value |= ${ID_TYPE_IDX}")
REQUEST=$(echo $REQUEST | jq ".callbacks[0].input[0].value |= \"${IDENTIFIER}\"")

do_api_request