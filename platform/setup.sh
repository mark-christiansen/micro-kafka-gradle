#!/bin/bash

echo "Crete volumes"
mkdir volumes/account-contact-stream
mkdir volumes/account-stream
mkdir volumes/contact-address-stream
mkdir volumes/contact-stream
mkdir volumes/grafana
mkdir volumes/kafka-1
mkdir volumes/postgres
mkdir volumes/prometheus
mkdir volumes/zoo-1

echo "Starting Kafka environment"
docker compose up -d