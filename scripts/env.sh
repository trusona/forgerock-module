#!/usr/bin/env bash

mkdir -p ~/.m2
cp data/settings.xml ~/.m2/settings.xml

cat <<EOF > ~/.npmrc
@trusona:registry=https://trusona.jfrog.io/trusona/api/npm/npm
//trusona.jfrog.io/trusona/api/npm/:_password="$(echo -ne $ARTIFACTORY_PASSWORD | base64 -w 0)"
//trusona.jfrog.io/trusona/api/npm/:username=$ARTIFACTORY_USERNAME
//trusona.jfrog.io/trusona/api/npm/:email=$ARTIFACTORY_EMAIL
//trusona.jfrog.io/trusona/api/npm/:always-auth=true
EOF
