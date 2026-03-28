FROM openjdk:22-jdk-slim AS builder
LABEL authors="damola.adekoya"

WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy only the pom.xml file to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B || echo "Failed to download dependencies, continuing..."

# Copy the rest of the source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:22-jdk-slim

WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]