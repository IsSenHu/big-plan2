#!/bin/bash
cd /data/docker-compose || exit
ls
if [ "$SERVER" == "all" ]; then
  echo "up all"
  uping=$(/usr/local/bin/docker-compose ps | grep Up | awk '{print $1}')
  echo "找到已经在运行的容器:"
  echo "$uping"
  if [ -n "$uping" ]; then
      /usr/local/bin/docker-compose down
  fi
  /usr/local/bin/docker-compose up -d
else
  echo "up $SERVER"
  uping=$(/usr/local/bin/docker-compose ps | grep "$SERVER" | grep Up | awk '{print $1}')
  echo "找到已经在运行的容器:"
  echo "$uping"
  if [ -n "$uping" ]; then
    /usr/local/bin/docker-compose down "$SERVER"
  fi
  /usr/local/bin/docker-compose up "$SERVER" -d
fi
