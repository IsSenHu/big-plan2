#!/bin/bash
cd /data/docker-compose || exit
ls
if [ "$SERVER" == "all" ]; then
  echo "down all"
  /usr/local/bin/docker-compose down
else
  echo "down $SERVER"
  /usr/local/bin/docker-compose down "$SERVER"
fi
