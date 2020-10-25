#!/usr/bin/env bash

set -e

CURRENT_DIR=$(pwd)
echo "CURRENT_DIR=$CURRENT_DIR"

docker-compose -f ./docker-compose.yml stop minio
docker-compose -f ./docker-compose.yml rm -f minio
docker-compose -f docker-compose.yml up -d minio localstack redis mongo
docker-compose -f docker-compose.yml up -d mysql

sleep 50

echo -e "Docker ps."
docker ps

