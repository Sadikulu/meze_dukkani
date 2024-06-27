FROM ubuntu:latest AS build

# Update package lists and install JDK
RUN apt-get update && apt-get install -y openjdk-17-jdk maven

# Set working directory
WORKDIR /app

# Copy the source code
COPY . .

# Build the project using Maven, skipping tests
RUN mvn clean install -DskipTests

# Use a slimmer image for runtime
FROM openjdk:17-jdk-slim

# Expose the application port
EXPOSE 8080

# Copy the built JAR file from the build stage
COPY --from=build /app/target/meze-0.0.1-SNAPSHOT.jar meze.jar

# Run the application
ENTRYPOINT ["java", "-jar", "meze.jar"]