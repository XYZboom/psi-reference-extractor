val kotlinVersion: String = rootProject.extra["versions.kotlin"] as String

plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":kt-references-analysis:analysis-api-impl-base"))
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
        optIn.addAll(listOf("kotlin.RequiresOptIn", "org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals"))
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}


