#!/bin/bash
cd /data/docker-compose || exit
ls
if [ "$SERVER" == "all" ]; then
  echo "stop all"
  /usr/local/bin/docker-compose stop
else
  echo "stop $SERVER"
  /usr/local/bin/docker-compose stop "$SERVER"
fi
