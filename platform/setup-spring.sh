#!/bin/bash

echo "Crete volumes"
mkdir volumes/account-contact-stream-1
mkdir volumes/account-contact-stream-2
mkdir volumes/account-contact-stream-3
mkdir volumes/account-contact-stream-4
mkdir volumes/account-stream-1
mkdir volumes/account-stream-2
mkdir volumes/account-stream-3
mkdir volumes/account-stream-4
mkdir volumes/contact-address-stream-1
mkdir volumes/contact-address-stream-2
mkdir volumes/contact-stream-1
mkdir volumes/contact-stream-2
mkdir volumes/contact-stream-3
mkdir volumes/contact-stream-4
mkdir volumes/grafana
mkdir volumes/kafka-1
mkdir volumes/kafka-2
mkdir volumes/postgres
mkdir volumes/prometheus
mkdir volumes/zoo-1

echo "Starting Kafka environment"
docker compose -f docker-compose-spring.yml up -d
