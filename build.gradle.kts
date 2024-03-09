plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

tasks.register<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

fun MavenPublication.configurePublication() {
    groupId = "com.github.xyzboom"
    artifactId = "psi-reference-extractor"
    version = "1.0.0-SNAPSHOT"
    pom {
        // 设置项目的元数据信息
        name.set("psi-reference-extractor")
        description.set("A useful library for extract psi reference")
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
allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://www.jetbrains.com/intellij-repository/releases")
    }
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    api(project(":intellij-core"))
    implementation("org.jetbrains:annotations:24.0.0")
    implementation(project(":kt-references-analysis:analysis-api"))
    implementation(project(":kt-references-analysis:analysis-api-fe10"))
    implementation(project(":kt-references-analysis:analysis-api-impl-base"))
    implementation(project(":kt-references-analysis:analysis-internal-utils"))
    implementation(project(":kt-references-analysis:kt-references-fe10"))
    implementation(project(":kt-references-analysis:project-structure"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223-unshaded:1.9.22")
}

tasks.test {
    useJUnitPlatform()
    inputs.dir("src/testData")
}
kotlin {
    jvmToolchain(17)
}