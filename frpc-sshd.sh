#!/bin/bash
#chkconfig: 2345 80 90
#description:auto_run
nohup /opt/frp_0.34.1_linux_amd64/frpc -c /opt/frp_0.34.1_linux_amd64/frpc.ini
echo "frpc is started"
docker run --detach --publish 8443:443 --publish 8888:80 --publish 8222:22 --name gitlab --restart unless-stopped --volume /usr/local/gitlab/etc:/etc/gitlab --volume /usr/local/gitlab/log:/var/log/gitlab --volume /usr/local/gitlab/data:/var/opt/gitlab --privileged=true gitlab/gitlab-ce
docker run -d --privileged=true -p 6379:6379 -v /data/redis/etc:/etc/redis -v /data/redis/data:/data --name redis redis redis-server /etc/redis/redis.conf --appendonly yes
