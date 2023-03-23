#!/bin/bash

BASE=$(dirname "$0")
cd ${BASE}
. ../env.sh

kafka-consumer-groups --command-config $KAFKA_CONFIG --bootstrap-server $BOOTSTRAP_URL --list
