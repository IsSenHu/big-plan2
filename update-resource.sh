#!/bin/bash
ls /data/docker-compose
rm -rf /data/docker-compose/docker-compose.yml
rm -rf /data/docker-compose/.env
cp ./docker-compose.yml /data/docker-compose
cp ./.env /data/docker-compose
