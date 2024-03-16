plugins {
    kotlin("jvm")
    application
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
    artifactId = "psi-reference-extractor-runner"
    version = "1.0.0-SNAPSHOT"
    pom {
        // 设置项目的元数据信息
        name.set("psi-reference-extractor-runner")
        description.set("Command runner for com.github.xyzboom:psi-reference-extractor")
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
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
    implementation("info.picocli:picocli:4.7.5")
    api(rootProject)
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