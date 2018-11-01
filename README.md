# offer-management
Offer management tool to create offer for Sunbets Vegas


RUN LOCAL DEV STACK in IntelliJ for debug purposeredd:

1.POM File comment out mysql and FLYWAY and enable H2 DB
2.add "-Dspring.profiles.active=test" to edit configuration VM Options field
3.Run normally or in Debug mode, this will use Spring boot plus H2 Database stack

Environment details:
API Service: localhost:8080
DB UI: localhost:8080/h2




RUNNING PROD STACK IN LOCAL with Docker:
1.POM File comment out H2 DB and enable MYSQL and FLYWAY dependency
2.on the repo folder, run `mvn package` to build the JAR File
3.run `docker build --no-cache -t offer-management .` to build image
4.run `docker-compose up` to bring up the environment.






DEVELOYING TO UAT manually:

Pre steps:
0.1 Build Jar file
mvn package
0.2 Build image
docker build --no-cache -t offer-management .


1.TAG image
docker tag offer-management:latest redbook-docker-dev.artifacts.tabdigital.com.au/offer-management:latest

2.Login to docker
docker login redbook-docker-dev.artifacts.tabdigital.com.au

3.Push image
docker push redbook-docker-dev.artifacts.tabdigital.com.au/offer-management:latest

4.Login to UAT Server
ssh luoc@10.12.72.6  - password luoc

5.Pull latest image
sudo docker pull redbook-docker-dev.artifacts.tabdigital.com.au/offer-management


6.Deploy image
sudo /usr/local/bin/docker-compose up