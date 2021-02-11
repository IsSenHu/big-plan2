#!/bin/bash
pwd
if [ "$OP" == "package" ]; then
  sh package.sh
elif [ "$OP" == "build" ]; then
  sh package.sh
  sh build.sh
elif [ "$OP" == "update" ]; then
  sh update-resource.sh
elif [ "$OP" == "down" ]; then
  sh down.sh
elif [ "$OP" == "stop" ]; then
  sh stop.sh
elif [ "$OP" == "start" ]; then
  sh package.sh
  sh start.sh
else
  sh package.sh
  sh up.sh
fi
