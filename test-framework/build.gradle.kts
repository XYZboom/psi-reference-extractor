plugins {
    kotlin("jvm")
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

val generateJvmTest = tasks.create<JavaExec>("GenerateJvmTest") {
    group = "generate"
    mainClass = "com.github.xyzboom.extractor.JvmReferenceInfoTestGenerator"
    group = "Generate"
    workingDir = rootProject.rootDir
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("line.separator", "\n")
}

rootProject.tasks.withType<Test> {
    dependsOn(generateJvmTest)
}