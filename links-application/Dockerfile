# Stage 1: Build the native executable
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder

WORKDIR /code

# Copy files with quarkus user ownership
COPY --chown=quarkus:quarkus gradle ./gradle
COPY --chown=quarkus:quarkus gradlew settings.gradle build.gradle env.gradle gradle.properties ./
COPY --chown=quarkus:quarkus links-application ./links-application

RUN ./gradlew :links-application:build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false --no-daemon

# Stage 2: Create the final lightweight image
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

WORKDIR /app

COPY --from=builder /code/links-application/build/*-runner /app/application

# Configure non-root user
RUN chown -R 1001:0 /app
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
