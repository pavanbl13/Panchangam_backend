# Multi-stage build for optimized Docker image
# Stage 1: Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Build the application (skip tests for faster build)
RUN mvn clean install -DskipTests
# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy JAR from builder stage
COPY --from=builder /app/target/sankalpam-api-1.0.0.jar .
# Copy lookup data (static reference data)
COPY --from=builder /app/src/main/resources/lookup ./src/main/resources/lookup
# Create logs directory
RUN mkdir -p /app/logs
# Expose port
EXPOSE 8081
# Health check using Spring Boot actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
# Set non-sensitive environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV LOGGING_LEVEL_ROOT="INFO"
ENV LOGGING_LEVEL_COM_SANKALPAM="DEBUG"
# Note: GOOGLE_API_KEY must be passed at runtime via -e flag, not in image
# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar sankalpam-api-1.0.0.jar"]
