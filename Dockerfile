# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------------------
# JDK-only image; the Gradle wrapper pins and fetches Gradle itself, so the host
# needs no local JDK or Gradle install.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copy the wrapper + build scripts first and warm up the Gradle distribution, so
# this layer is cached and only re-runs when the build configuration changes.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon --version

# Then the sources. Tests run in CI / `./gradlew test`; the image build skips them.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ------------------------------------------------------------
# Slim JRE-only image; runs as a non-root user.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

# bootJar is configured to emit a fixed name (app.jar), so no globbing here.
COPY --from=build /build/build/libs/app.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8080

# Container-aware health check hitting the Actuator liveness probe.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
