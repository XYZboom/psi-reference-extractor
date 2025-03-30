val kotlinVersion: String = rootProject.extra["versions.kotlin"] as String

plugins {
    kotlin("jvm")
    application
}

dependencies {
    api(project(":intellij-core"))
    api(project(":kt-references-analysis:project-structure"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
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
    compilerOptions {
        optIn.addAll(listOf(
            "org.jetbrains.kotlin.analysis.api.KaImplementationDetail",
            "org.jetbrains.kotlin.analysis.api.KaNonPublicApi",
            "org.jetbrains.kotlin.analysis.api.KaIdeApi",
            "org.jetbrains.kotlin.analysis.api.KaExperimentalApi",
            "org.jetbrains.kotlin.analysis.api.KaPlatformInterface" // Platform interface is not stable yet
        ))
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
