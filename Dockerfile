FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src .
# Kaynak kodunu çalışma dizinine kopyalayın

RUN apt-get update && apt-get install -y build-essential openjdk-17-jdk maven

RUN cd /app

RUN mvn clean install

COPY target/meze-0.0.1-SNAPSHOT.jar meze.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "meze.jar"]
