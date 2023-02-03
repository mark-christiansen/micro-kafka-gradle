#!/bin/bash

echo "Crete volumes"
mkdir volumes/kafka-1
mkdir volumes/postgres
mkdir volumes/zoo-1

echo "Starting Kafka environment"
docker compose up -d