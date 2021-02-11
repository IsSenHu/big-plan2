#!/bin/bash
buildOne() {
  echo "$1"
  CURRENT="$1"
  cd "$CURRENT" || exit
  /opt/apache-maven-3.6.3/bin/mvn clean package -Dmaven.test.skip=true
  cd target || exit
  if [ ! -d "/data/$CURRENT" ]; then
      mkdir -p /data/"$CURRENT"
  fi
  if [ -f "/data/$CURRENT/$CURRENT.jar" ]; then
      mv "/data/$CURRENT/$CURRENT.jar" "/data/$CURRENT/$CURRENT.jar.bak.$BAK_TIME"
  fi
  cp "$CURRENT".jar /data/"$CURRENT"
  cd ../../
  pwd
  if [ -f "/data/$CURRENT/Dockerfile" ]; then
      mv "/data/$CURRENT/Dockerfile" "/data/$CURRENT/Dockerfile.bak.$BAK_TIME"
  fi
  cp ./Dockerfile /data/"$CURRENT"
  if [ -f "/data/$CURRENT/wait-for.sh" ]; then
      mv "/data/$CURRENT/wait-for.sh" "/data/$CURRENT/wait-for.sh.bak.$BAK_TIME"
  fi
  cp ./wait-for.sh /data/"$CURRENT"
  echo "build $CURRENT success"
}

buildInner() {
  echo "$1 $2"
  cd "$1" || exit
  cd "$2" || exit
  /opt/apache-maven-3.6.3/bin/mvn clean package -Dmaven.test.skip=true
  cd target || exit
  if [ ! -d "/data/$2" ]; then
      mkdir -p /data/"$2"
  fi
  if [ -f "/data/$2/$2.jar" ]; then
      mv "/data/$2/$2.jar" "/data/$2/$2.jar.bak.$BAK_TIME"
  fi
  cp "$2".jar /data/"$2"
  cd ../../../
  pwd
  if [ -f "/data/$2/Dockerfile" ]; then
      mv "/data/$2/Dockerfile" "/data/$2/Dockerfile.bak.$BAK_TIME"
  fi
  cp ./Dockerfile /data/"$2"
  if [ -f "/data/$2/wait-for.sh" ]; then
      mv "/data/$2/wait-for.sh" "/data/$2/wait-for.sh.bak.$BAK_TIME"
  fi
  cp ./wait-for.sh /data/"$2"
  echo "build $2 success"
}

building() {
  GATEWAY=gateway
  AC=ac
  AC_SERVER=auth-center-server
  MM=money-management
  MM_SERVER=money-management-server
  BLOG=blog
  BLOG_ADMIN=blog-admin
  BLOG_SERVER=blog-server
  if [ "$1" == 'all' ]; then
    echo 'build all'
    buildInner "$AC" "$AC_SERVER"
    buildOne "$GATEWAY"
    buildInner "$MM" "$MM_SERVER"
    buildInner "$BLOG" "$BLOG_ADMIN"
    buildInner "$BLOG" "$BLOG_SERVER"
  elif [ "$1" == 'no' ]; then
    echo "no"
  else
    echo "build $1"
    if [ "$1" == "$GATEWAY" ]; then
      buildOne "$1"
    else
      if [ "$DIR" == '' ]; then
        echo "project dir is empty!!!"
        exit
      fi
      buildInner "$DIR" "$1"
    fi
  fi
  if [ ! -d "/data/docker-compose" ]; then
      mkdir -p /data/docker-compose
  fi
  ls /data/docker-compose
  rm -rf /data/docker-compose/docker-compose.yml
  rm -rf /data/docker-compose/.env
  cp ./docker-compose.yml /data/docker-compose
  cp ./.env /data/docker-compose
  echo "finish build"
}

BAK_TIME=$(date '+%Y%m%d-%H:%M:%S')
building "$SERVER"

