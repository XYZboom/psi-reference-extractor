import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register<Jar>("sourcesJar") {
    from(subprojects.map { File(it.projectDir, "src/main/kotlin") })
    archiveClassifier.set("sources")
}
fun MavenPublication.configurePublication() {
    groupId = "com.github.xyzboom"
    artifactId = "kt-reference-analysis-for-psi-reference-extractor"
    version = "1.0.0-SNAPSHOT"
    pom {
        // 设置项目的元数据信息
        name.set("kt-reference-analysis-for-psi-reference-extractor")
        description.set("kotlin reference analysis for com.github.xyzboom:psi-reference-extractor")
        url.set("https://github.com/XYZboom/psi-reference-extractor")
    }
}

publishing {
    publications {
        // 配置发布的库
        create<MavenPublication>("jar") {
            from(components["java"])
            artifacts {
                archives(tasks.named<ShadowJar>("shadowJar"))
                archives(tasks["sourcesJar"]) {
                    classifier = "sources"
                }
            }
            configurePublication()
        }
    }

    repositories {
        // 配置发布到的 Maven 仓库
        maven {
            url = uri("https://maven.pkg.github.com/xyzboom/psi-reference-extractor")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    api(project(":intellij-core"))
    api(project(":kt-references-analysis:analysis-api"))
    api(project(":kt-references-analysis:analysis-api-fe10"))
    api(project(":kt-references-analysis:analysis-api-impl-base"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api(project(":kt-references-analysis:kt-references-fe10"))
    api(project(":kt-references-analysis:project-structure"))
}