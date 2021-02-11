mkdir /data/mysql
docker run --name my-mysql -v /data/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=521428Slyt! -d mysql:5.7.33 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
