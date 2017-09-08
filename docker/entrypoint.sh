#!/bin/sh

set -e

print_message() {
    echo
    echo "*********************************"
    echo "* $1"
    echo "*********************************"
    echo
}

DATA_DIR="/var/lib/mysql"

print_message 'Initializing MySQL...'

if [ ! -d "$DATA_DIR/mysql" ]; then
    mkdir -p "$DATA_DIR"
    chown -R mysql:mysql "$DATA_DIR"

    mysql_install_db --user=mysql --datadir="$DATA_DIR" --rpm
fi

mysqld_safe --nowatch

print_message 'Waiting for MySQL to start...'

# wait until mysql is available...
while ! echo exit | nc 127.0.0.1 3306 </dev/null >/dev/null; do sleep 5; done

print_message 'Creating the Feed database...'

mysql -uroot -h 127.0.0.1 <<-EOF
   CREATE DATABASE IF NOT EXISTS feed_db;
   GRANT ALL ON feed_db.* TO 'usr'@'localhost' IDENTIFIED BY 'pwd';
EOF

print_message 'Starting the service...'

java -jar /opt/hal-feed-server/hal-feed-server.jar
