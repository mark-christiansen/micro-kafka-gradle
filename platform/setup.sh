#!/bin/bash

echo "Crete volumes"
mkdir volumes/account-contact-stream-1
mkdir volumes/account-stream-1
mkdir volumes/contact-address-stream-1
mkdir volumes/contact-stream-1
mkdir volumes/grafana
mkdir volumes/kafka-1
mkdir volumes/postgres
mkdir volumes/prometheus
mkdir volumes/zoo-1

echo "Starting Kafka environment"
docker compose up -d
