FROM openjdk:17-alpine

WORKDIR /app

COPY target/*.jar app.jar

RUN apk add --no-cache ca-certificates

RUN wget -O /opt/jdk-17.tar.gz https://download.java.net/openjdk/17.0.3/jdk-17.0.3-linux-x64.tar.gz
RUN tar -xvf /opt/jdk-17.tar.gz

ENV JAVA_HOME=/opt/jdk-17
PATH=$JAVA_HOME/bin:$PATH

EXPOSE 8080

ENTRYPOINT ["/opt/jdk-17/bin/java", "-jar", "app.jar"]