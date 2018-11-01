#!/bin/sh


# we need to make sure the DB is up before spinning up API service
echo "Wait until the database is up..."
i=0
until mysqladmin ping -u${DATABASE_USER} -p${DATABASE_PASSWORD} -h ${DATABASE_HOST} 2>/dev/null 1>/dev/null; do
    sleep 1
    i=$((i+1))
    if [ "$i" -gt 320 ]; then
        echo "The database container failed to come up"
        exit 1
    fi
done

java -Dnewrelic.config.file=newrelic/newrelic.yml -Dnewrelic.config.license_key=${NEWRELICLICENSEKEY} -Dnewrelic.config.app_name="${NEWRELICAPPNAME}" -Dnewrelic.environment=staging -javaagent:newrelic/newrelic.jar -Djava.security.egd=file:/dev/./urandom -jar /app.jar