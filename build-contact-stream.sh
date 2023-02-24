#!/bin/bash

VERSION="1.0-SNAPSHOT"
APP="contact-stream"

echo ""
echo "build the fat jars for ${APP}"
./gradlew :${APP}:assemble

echo ""
echo "setup environment variables for the ${APP} to be run"
export APP_ID=${APP}
export BOOTSTRAP_URL=localhost:9092
export CONTACT_ADDRESS_TOPIC=canonical.contactaddress
export CONTACT_TOPIC=source.contact
export ERROR_TOPIC=error.contact
export GROUP_ID=${APP}
export HTTP_PORT=8802
export IN_MEMORY_STATE=false
export OUTPUT_TOPIC=canonical.contact
export SCHEMA_URL=http://localhost:8081
export STATE_STORE_CLEANUP=false

echo ""
echo "startup ${APP} with the native-image-agent to collect the native-image configuration"
java -agentlib:native-image-agent=config-merge-dir=${APP}/src/main/resources/META-INF/native-image/com.mycompany/${APP} \
-cp ${APP}/src/main/resources -jar ${APP}/build/libs/${APP}-${VERSION}-all.jar