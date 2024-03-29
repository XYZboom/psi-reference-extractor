import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies{
    implementation(project(":kt-references-analysis:extra-references"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}

sourceSets {
    main {

    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}