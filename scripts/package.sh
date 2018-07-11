#!/bin/bash
# Build and zip up a package of the forgerock module
# run ./gradlew clean build first.
set -e
PROJECT="trusona-forgerock-module"
PLUGIN_VERSION=$(./gradlew properties | grep version | cut -f2 -d: | tr -d ' ')
PKG_DIR="$PROJECT-${PLUGIN_VERSION}"
ZIP_FILE="${PKG_DIR}.zip"
FINAL_JAR_NAME="$PROJECT-${PLUGIN_VERSION}.jar"

# Artifacts
JAR="libs/$PROJECT-${PLUGIN_VERSION}-all.jar"
PDF="README.pdf"

if [ -n "${SKIP_BUILD}" ]; then
    echo "Skipping build command"
else
    ./gradlew clean build
fi

cd build

mkdir "${PKG_DIR}"

cp $JAR "${PKG_DIR}/${FINAL_JAR_NAME}"
cp $PDF $PKG_DIR

zip -r $ZIP_FILE $PKG_DIR
echo "Created package $ZIP_FILE"
cd ..