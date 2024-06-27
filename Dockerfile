FROM temurin:17-alpine AS builder

WORKDIR /app

COPY target/*.jar app.jar  # JAR dosyasını builder aşamasından önce kopyalayın

RUN apk add --no-cache openssl

# COPY --from=builder /app/app.jar app.jar  - Bu satırı silin - gereksiz

FROM openjdk:17-alpine

WORKDIR /app

COPY app.jar .

ENV JAVA_HOME=/opt/jdk-17
ENV PATH="$JAVA_HOME/bin:$PATH"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
