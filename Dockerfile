FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY src .

RUN apt-get install maven -y
RUN mvn -X clean install

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /target/meze-0.0.1-SNAPSHOT.jar meze.jar

ENTRYPOINT ["java","-jar","meze.jar"]