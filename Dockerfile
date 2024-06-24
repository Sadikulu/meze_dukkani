#
# Build stage
#
FROM maven:3.8.5-jdk-17 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:17
COPY --from=build /target/meze-0.0.1-SNAPSHOT.jar meze.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","meze.jar"]