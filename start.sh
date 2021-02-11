#!/bin/bash
cd /data/docker-compose || exit
ls
if [ "$SERVER" == "all" ]; then
  echo "start all"
  uping=$(/usr/local/bin/docker-compose ps | grep Up | awk '{print $1}')
  echo "找到已经在运行的容器:"
  echo "$uping"
  if [ -n "$uping" ]; then
      /usr/local/bin/docker-compose stop
  fi
  /usr/local/bin/docker-compose start
else
  echo "start $SERVER"
  uping=$(/usr/local/bin/docker-compose ps | grep "$SERVER" | grep Up | awk '{print $1}')
  echo "找到已经在运行的容器:"
  echo "$uping"
  if [ -n "$uping" ]; then
    /usr/local/bin/docker-compose stop "$SERVER"
  fi
  /usr/local/bin/docker-compose start "$SERVER"
fi
