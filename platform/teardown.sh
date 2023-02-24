#!/bin/bash

echo "Stopping Kafka environment"
docker compose down

echo "Cleaning up volumes"
find volumes/account-contact-stream-1 -mindepth 1 -delete
find volumes/account-stream-1 -mindepth 1 -delete
find volumes/contact-address-stream-1 -mindepth 1 -delete
find volumes/contact-stream-1 -mindepth 1 -delete
find volumes/grafana -mindepth 1 -delete
find volumes/kafka-1 -mindepth 1 -delete
find volumes/postgres -mindepth 1 -delete
find volumes/prometheus -mindepth 1 -delete
find volumes/zoo-1 -mindepth 1 -delete
