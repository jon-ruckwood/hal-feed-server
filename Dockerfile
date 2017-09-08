FROM openjdk:7-alpine

MAINTAINER QMetric Group Ltd <no-reply@qmetric.com>

RUN apk add --update mysql mysql-client && rm -f /var/cache/apk/*

COPY target/hal-feed-server.jar /opt/hal-feed-server/hal-feed-server.jar
COPY docker/config.yml /usr/local/config/hal-feed-server/server-config.yml

COPY docker/entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080 8081

ENTRYPOINT ["/docker-entrypoint.sh"]
