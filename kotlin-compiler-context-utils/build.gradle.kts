plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}
fun MavenPublication.configurePublication() {
    groupId = "com.github.xyzboom"
    artifactId = "kotlin-compiler-context-utils-for-psi-reference-extractor"
    version = "1.0.0-SNAPSHOT"
    pom {
        // 设置项目的元数据信息
        name.set("kotlin-compiler-context-utils-for-psi-reference-extractor")
        description.set("Kotlin compiler context utils for com.github.xyzboom:psi-reference-extractor")
        url.set("https://github.com/XYZboom/psi-reference-extractor")

        // 配置项目的许可证信息
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        // 配置项目的开发者信息
        developers {
            developer {
                id.set("Xiaotian Ma")
                name.set("Xiaotian Ma")
                email.set("xyzboom@qq.com")
            }
        }
    }
}

publishing {
    publications {
        // 配置发布的库
        create<MavenPublication>("jar") {
            from(components["java"])
            artifacts {
                archives(tasks["jar"])
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
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    api(project(":intellij-core"))
    api(project(":kt-references-analysis"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}