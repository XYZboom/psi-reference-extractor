val kotlinVersion: String = rootProject.extra["versions.kotlin"] as String

plugins {
    kotlin("jvm")
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}