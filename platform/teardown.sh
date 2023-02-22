#!/bin/bash

echo "Stopping Kafka environment"
docker compose down

echo "Cleaning up volumes"
find volumes/account-contact-stream -mindepth 1 -delete
find volumes/account-stream -mindepth 1 -delete
find volumes/contact-address-stream -mindepth 1 -delete
find volumes/contact-stream -mindepth 1 -delete
find volumes/grafana -mindepth 1 -delete
find volumes/kafka-1 -mindepth 1 -delete
find volumes/postgres -mindepth 1 -delete
find volumes/prometheus -mindepth 1 -delete
find volumes/zoo-1 -mindepth 1 -delete