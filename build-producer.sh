#!/bin/bash

VERSION="1.0-SNAPSHOT"
APP="producer"

echo ""
echo "build the fat jars for ${APP}"
./gradlew :${APP}:assemble

echo ""
echo "setup environment variables for the ${APP} to be run"
export ACCOUNT_CONTACT_TOPIC=source.accountcontact
export ACCOUNT_TOPIC=source.account
export ACCOUNTS=100
export ADDRESSES_PER_CONTACT=1
export BATCH_SIZE=10
export BOOTSTRAP_URL=localhost:9092
export CANONICAL_ACCOUNT_CONTACT_TOPIC=canonical.accountcontact
export CANONICAL_ACCOUNT_TOPIC=canonical.account
export CANONICAL_CONTACT_ADDRESS_TOPIC=canonical.contactaddress
export CANONICAL_CONTACT_TOPIC=canonical.contact
export CLIENT_ID=${APP}
export CONTACT_ADDRESS_TOPIC=source.contactaddress
export CONTACT_ROLE_TOPIC=source.contactrole
export CONTACT_TOPIC=source.contact
export CONTACTS_PER_ACCOUNT=1
export COUNTRY_TOPIC=source.country
export FREQUENCY_MS=100
export HTTP_PORT=8800
export PARTITIONS=5
export SCHEMA_URL=http://localhost:8081
export SCHEMAS_PATH=schemas/source
export STATE_TOPIC=source.state
export THREADS=5

echo ""
echo "startup ${APP} with the native-image-agent to collect the native-image configuration"
java -agentlib:native-image-agent=config-merge-dir=${APP}/src/main/resources/META-INF/native-image/com.mycompany/${APP} \
-cp ${APP}/src/main/resources -jar ${APP}/build/libs/${APP}-${VERSION}-all.jar