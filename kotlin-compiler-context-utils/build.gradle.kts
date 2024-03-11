plugins {
    kotlin("jvm")
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    api(project(":intellij-core"))
    api(project(":kt-references-analysis:analysis-api"))
    api(project(":kt-references-analysis:analysis-api-fe10"))
    api(project(":kt-references-analysis:analysis-api-impl-base"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api(project(":kt-references-analysis:kt-references-fe10"))
    api(project(":kt-references-analysis:project-structure"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}