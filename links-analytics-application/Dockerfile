# Stage 1: Build the application
FROM gradle:8.14-jdk21-alpine AS builder

WORKDIR /app

COPY settings.gradle build.gradle env.gradle gradle.properties ./
COPY links-analytics-application ./links-analytics-application

RUN gradle :links-analytics-application:bootJar --no-daemon

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/links-analytics-application/build/libs/*.jar app.jar

# Create a non-root user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
