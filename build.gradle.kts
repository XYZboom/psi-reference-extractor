plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
}

group = "com.github.xyzboom"
version = "1.0.0-SNAPSHOT"

publishing {
    publications {
        // 配置发布的库
        create<MavenPublication>("maven") {
            // 设置发布的组织、模块和版本信息
            groupId = "com.github.xyzboom"
            artifactId = "psi-reference-extractor"
            version = "1.0.0-SNAPSHOT"

            // 配置发布的产物
            from(components["java"])

            // 配置发布的元数据
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
    }

    repositories {
        // 配置发布到的 Maven 仓库
        maven {
            url = uri("https://maven.pkg.github.com/xyzboom/psi-reference-extractor")
            credentials {
                // 配置访问仓库所需的凭据
                println(System.getenv("GITHUB_ACTOR"))
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}