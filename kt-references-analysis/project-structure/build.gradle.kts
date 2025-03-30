val kotlinVersion: String = rootProject.extra["versions.kotlin"] as String

plugins {
    kotlin("jvm")
    application
}

dependencies{
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
}

sourceSets {
    main {

    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}
