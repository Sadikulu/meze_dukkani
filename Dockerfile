# Bu satırı silin
# COPY target/*.jar app.jar

FROM temurin:17-alpine AS build

WORKDIR /app

COPY target/*.jar app.jar

RUN apk add --no-cache openssl

COPY --from=build /app/app.jar app.jar

FROM openjdk:17-alpine

WORKDIR /app

COPY app.jar .

ENV JAVA_HOME=/opt/jdk-17
ENV PATH="$JAVA_HOME/bin:$PATH"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
