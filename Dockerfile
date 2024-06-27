FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src .
# Kaynak kodunu çalışma dizinine kopyala

# Maven ile build yapmadan önce gerekli bağımlılıkları kurun (gerekirse)
RUN apt-get update && apt-get install -y \build-essential \openjdk-17-jdk \maven

# Maven build işlemini gerçekleştirmek için projenizin kök dizinine gidin
RUN cd /app

# Maven build'u çalıştırın
RUN mvn clean install

# Oluşturulan JAR dosyasını kopyalayın
COPY app/target/meze-0.0.1-SNAPSHOT.jar meze.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "meze.jar"]
