FROM openjdk:17-jdk-slim

WORKDIR /app

# Proje kök dizinindeki tüm dosyaları kopyala (src, pom.xml vs.)
COPY . .

# Maven ile build yapmadan önce gerekli bağımlılıkları kurun (gerekirse)
RUN apt-get update && apt-get install -y \
    build-essential \
    maven

# Maven build işlemini gerçekleştirmek için projenizin kök dizinine gidin ve build işlemini çalıştırın
RUN mvn clean install

# Oluşturulan JAR dosyasını kopyalayın
COPY target/meze-0.0.1-SNAPSHOT.jar meze.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "meze.jar"]
