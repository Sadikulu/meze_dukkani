# Aşama 1: Derleme aşaması
FROM maven:3.6.3-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Aşama 2: Çalışma aşaması
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/meze-0.0.1-SNAPSHOT /app/meze.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "meze.jar"]
