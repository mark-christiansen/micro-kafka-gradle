#!/bin/bash

VERSION="1.0-SNAPSHOT"

echo ""
echo "build the fat jars for producer"
./gradlew :producer:assemble

echo ""
echo "setup environment variables for the producer to be run"
export BOOTSTRAP_URL="localhost:9092"
export SCHEMA_URL="http://localhost:8081"
export CLIENT_ID="kafka-producer"
export MESSAGES=1
export SCHEMA="customer"
export FREQUENCY_MS=1000
export BATCH_SIZE=1
export SCHEMAS_PATH="/Users/markchristiansen/repos/github.com/mark-christiansen/micro-kafka/producer/src/main/resources/schemas"

echo ""
echo "startup producer with the native-image-agent to collect the native-image configuration"
"${GRAALVM_HOME}"/bin/java \
-agentlib:native-image-agent=config-merge-dir=producer/src/main/resources/META-INF/native-image/com.mycompany/producer \
-cp producer/src/main/resources -jar producer/build/libs/producer-${VERSION}-all.jar

echo ""
echo "build the fat jars for streams"
./gradlew :streams:assemble

echo ""
echo "setup environment variables for the streams to be run"
export APP_ID="kafka-streams"
export STREAM_TYPE="stateless"
export INPUT_TOPIC="customer"
export OUTPUT_TOPIC="customer-transformed"
export ERROR_TOPIC="customer-error"

echo ""
echo "startup streams with the native-image-agent to collect the native-image configuration"
nohup "${GRAALVM_HOME}"/bin/java \
-agentlib:native-image-agent=config-merge-dir=streams/src/main/resources/META-INF/native-image/com.mycompany/streams \
-jar streams/build/libs/streams-${VERSION}-all.jar &
PID=$!
sleep 5
kill $PID

java -agentlib:native-image-agent=config-merge-dir=producer/src/main/resources/META-INF/native-image/com.mycompany/producer -cp producer/src/main/resources -jar producer/build/libs/producer-1.0-SNAPSHOT-all.jar
java -agentlib:native-image-agent=config-merge-dir=streams/src/main/resources/META-INF/native-image/com.mycompany/streams -cp streams/src/main/resources -jar streams/build/libs/streams-1.0-SNAPSHOT-all.jar
java -agentlib:native-image-agent=config-merge-dir=consumer/src/main/resources/META-INF/native-image/com.mycompany/consumer -cp consumer/src/main/resources -jar consumer/build/libs/consumer-1.0-SNAPSHOT-all.jar


echo ""
echo "build the fat jars for consumer"
./gradlew :consumer:assemble

echo ""
echo "setup environment variables for the consumer to be run"
export TOPIC="customer.transformed"
export CLIENT_ID="kafka-consumer"
export GROUP_ID="kafka-consumer"

echo ""
echo "startup consumer with the native-image-agent to collect the native-image configuration"
nohup "${GRAALVM_HOME}"/bin/java \
-agentlib:native-image-agent=config-merge-dir=consumer/src/main/resources/META-INF/native-image/com.mycompany/consumer \
-jar consumer/build/libs/consumer-${VERSION}-all.jar &
PID=$!
sleep 5
kill $PID

java -agentlib:native-image-agent=config-merge-dir=streams-stateful/src/main/resources/META-INF/native-image/com.mycompany/streams-stateful -jar streams-stateful/build/libs/streams-stateful-1.0-SNAPSHOT-all.jar
