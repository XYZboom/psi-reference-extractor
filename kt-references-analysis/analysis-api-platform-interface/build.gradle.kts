val kotlinVersion: String = rootProject.extra["versions.kotlin"] as String

plugins {
    kotlin("jvm")
    application
}

dependencies{
    implementation("io.opentelemetry:opentelemetry-api:1.41.0")
    api(project(":intellij-core"))
    api(project(":kt-references-analysis:analysis-api"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
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
    compilerOptions {
        optIn.addAll(listOf(
            "org.jetbrains.kotlin.analysis.api.KaImplementationDetail",
            "org.jetbrains.kotlin.analysis.api.KaNonPublicApi",
            "org.jetbrains.kotlin.analysis.api.KaIdeApi",
            "org.jetbrains.kotlin.analysis.api.KaExperimentalApi",
            "org.jetbrains.kotlin.analysis.api.KaPlatformInterface" // Platform interface is not stable yet
        ))
    }
}