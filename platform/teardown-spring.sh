#!/bin/bash

echo "Stopping Kafka environment"
docker compose -f docker-compose-spring.yml down

echo "Cleaning up volumes"
find volumes/account-contact-stream-1 -mindepth 1 -delete
find volumes/account-contact-stream-2 -mindepth 1 -delete
find volumes/account-contact-stream-3 -mindepth 1 -delete
find volumes/account-contact-stream-4 -mindepth 1 -delete
find volumes/account-stream-1 -mindepth 1 -delete
find volumes/account-stream-2 -mindepth 1 -delete
find volumes/account-stream-3 -mindepth 1 -delete
find volumes/account-stream-4 -mindepth 1 -delete
find volumes/contact-address-stream-1 -mindepth 1 -delete
find volumes/contact-address-stream-2 -mindepth 1 -delete
find volumes/contact-stream-1 -mindepth 1 -delete
find volumes/contact-stream-2 -mindepth 1 -delete
find volumes/contact-stream-3 -mindepth 1 -delete
find volumes/contact-stream-4 -mindepth 1 -delete
find volumes/grafana -mindepth 1 -delete
find volumes/kafka-1 -mindepth 1 -delete
find volumes/kafka-2 -mindepth 1 -delete
find volumes/postgres -mindepth 1 -delete
find volumes/prometheus -mindepth 1 -delete
find volumes/zoo-1 -mindepth 1 -delete
