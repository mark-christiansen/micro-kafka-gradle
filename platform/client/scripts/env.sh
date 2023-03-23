#!/bin/bash

CONFLUENT_LOCAL_HOME="~/programs/confluent-7.3.2"

if [[ $PATH != *"$CONFLUENT_LOCAL_HOME"* ]]; then
  PATH=$PATH:$CONFLUENT_LOCAL_HOME/bin
  export PATH
fi

DOMAIN="mycompany.com"
BOOTSTRAP_URL="kafka1.${DOMAIN}:29092"
SCHEMA_URL="http://schema1.${DOMAIN}:8081"

mkdir -p /conf

cat > /conf/kafka.properties <<EOF
bootstrap.servers=${BOOTSTRAP_URL}
security.protocol=PLAINTEXT
EOF

KAFKA_CONFIG=/conf/kafka.properties