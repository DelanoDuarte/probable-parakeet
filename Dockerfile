# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------------------
# Uses a Maven + JDK 21 image so the build needs no local JDK/Maven on the host.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies first: copy only the POM, resolve, then copy sources.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ------------------------------------------------------------
# Slim JRE-only image; runs as a non-root user.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8080

# Container-aware health check hitting the Actuator liveness probe.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
