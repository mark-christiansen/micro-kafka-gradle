#!/bin/bash

echo "Stopping Kafka environment"
docker compose down

echo "Cleaning up volumes"
find volumes/kafka-1 -mindepth 1 -delete
find volumes/postgres -mindepth 1 -delete
find volumes/zoo-1 -mindepth 1 -delete
find volumes/kstream-1 -mindepth 1 -delete