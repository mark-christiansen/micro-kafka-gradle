#!/bin/bash

BASE=$(dirname "$0")
cd ${BASE}
. ../env.sh

[[ -z "$1" ]] && { echo "Topic not specified" ; exit 1; }
TOPIC=$1

kafka-topics --command-config $KAFKA_CONFIG --bootstrap-server $BOOTSTRAP_URL --delete --topic $TOPIC
