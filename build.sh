#!/bin/bash
cd /data/docker-compose || exit
ls
if [ "$SERVER" == "all" ]; then
  echo "build all"
  /usr/local/bin/docker-compose build
else
  echo "build $SERVER"
  /usr/local/bin/docker-compose build "$SERVER"
fi
