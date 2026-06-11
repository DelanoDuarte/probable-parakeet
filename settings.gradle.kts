plugins {
    // Lets the Gradle toolchain auto-provision JDK 21 if it isn't already installed,
    // so `./gradlew` works on a fresh machine with only the wrapper.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "appointment-scheduling"
