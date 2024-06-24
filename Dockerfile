#
# Build stage
#
FROM maven:3.8.1-jdk-11 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:18.0.2.1
COPY --from=build /target/meze-0.0.1-SNAPSHOT.jar meze.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","meze.jar"]