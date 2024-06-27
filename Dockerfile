# Copy JAR file before building
COPY target/*.jar app.jar

FROM temurin:17-alpine AS builder

WORKDIR /app

RUN apk add --no-cache openssl

# No need to copy JAR here, it's already copied

FROM openjdk:17-alpine

WORKDIR /app

COPY app.jar .
# Copy the JAR from the context directory

ENV JAVA_HOME=/opt/jdk-17
ENV PATH="$JAVA_HOME/bin:$PATH"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]