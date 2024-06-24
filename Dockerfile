# Temel imaj olarak resmi OpenJDK imajını kullan
FROM openjdk:11-jre-slim

# Uygulamanın çalışacağı dizini belirle
WORKDIR /app

# Maven imajını kullanarak uygulamayı derle
# Bu aşamada uygulama kaynak kodunu kopyalayın ve derleyin
FROM maven:3.6.3-openjdk-11-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Derlenen JAR dosyasını hedef imajımıza kopyala
COPY --from=builder /target/meze-0.0.1-SNAPSHOT.jar meze.jar

# Uygulamanın çalıştırılacağı portu belirt
EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "meze.jar"]
