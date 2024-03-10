plugins {
    kotlin("jvm")
    application
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
    implementation("info.picocli:picocli:4.7.5")
    implementation(rootProject)
    implementation(project(":kotlin-compiler-context-utils"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
application {
    mainClass = "com.github.xyzboom.extractor.MainKt"
}