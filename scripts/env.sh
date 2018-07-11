#!/bin/bash

mkdir -p ~/.m2
cp data/settings.xml ~/.m2/settings.xml

cat <<EOF > ~/.npmrc
@trusona:registry=https://trusona.jfrog.io/trusona/api/npm/npm
//trusona.jfrog.io/trusona/api/npm/:_password="$(echo -ne $artifactory_password | base64 -w 0)"
//trusona.jfrog.io/trusona/api/npm/:username=$artifactory_username
//trusona.jfrog.io/trusona/api/npm/:email=$artifactory_email
//trusona.jfrog.io/trusona/api/npm/:always-auth=true
EOF