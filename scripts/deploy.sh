#!/usr/bin/env bash

set -e

script_dir=$(dirname $0)

./gradlew artifactoryPublish && ${script_dir}/deploy-github.sh
