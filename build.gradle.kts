import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.uphill"
version = "0.1.0"
description = "Medical appointment scheduling service (Portugal) — DDD + Spring Modulith"

extra["springModulithVersion"] = "2.0.6"
extra["springdocVersion"] = "3.0.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        // Aligns every Spring Modulith artifact to one version, the same way the
        // Spring Boot BOM (applied by the Boot plugin) aligns Spring/Jackson/etc.
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
    }
}

dependencies {
    // Web / REST
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    // Mail (real abstraction; transport is pluggable, see EmailSender)
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Observability / operations
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Spring Modulith: module model, persisted event publication registry, actuator endpoint
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")

    // OpenAPI / Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Stable, predictable artifact name so the Docker runtime stage can copy it
// without globbing and without a "-plain" jar getting in the way.
tasks.named<BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}

// The plain (non-executable) jar is unnecessary for a deployable service.
tasks.named<Jar>("jar") {
    enabled = false
}
