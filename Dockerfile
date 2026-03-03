# Multi-stage build for optimized Docker image

# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY src/main/frontend/package.json src/main/frontend/package-lock.json* ./
RUN npm ci --production=false
COPY src/main/frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Copy built frontend into static resources
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static/
# Build the application (skip tests and frontend plugin since we already built it)
RUN mvn clean install -DskipTests -Dfrontend.skip=true
# Capture the built JAR name for the runtime stage
RUN JAR_FILE=$(ls /app/target/*.jar | grep -v original) && \
    cp $JAR_FILE /app/target/app.jar

# Stage 3: Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy JAR from builder stage (renamed to app.jar to avoid version coupling)
COPY --from=builder /app/target/app.jar .
# Copy lookup data (static reference data)
COPY --from=builder /app/src/main/resources/lookup ./src/main/resources/lookup
# Create logs directory
RUN mkdir -p /app/logs
# Expose port
EXPOSE 8000
# Health check using Spring Boot actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1
# Set non-sensitive environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV LOGGING_LEVEL_ROOT="INFO"
ENV LOGGING_LEVEL_COM_SANKALPAM="DEBUG"
# Note: GEOSEARCH_API_KEY must be passed at runtime via -e flag, not in image
# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
