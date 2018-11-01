FROM redbook-docker-dev.artifacts.tabdigital.com.au/wagerplayer-openjdk:8u131-jdk-alpine
MAINTAINER Chris Luo<chris.luo@tabcorp.com.au>

VOLUME /tmp
COPY target/offer-management-1.0.jar app.jar
COPY target/newrelic /newrelic
COPY run.sh /run.sh
EXPOSE 8080

RUN apk update && apk add mysql-client

ENTRYPOINT ["/run.sh"]