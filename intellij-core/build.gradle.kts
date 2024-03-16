plugins {
    `java-library`
    `maven-publish`
}

val intellijVersion = "232.10300.40"

//https://jetbrains.team/p/ij/repositories/intellij/files/aea53cfc5d27b15246ab7b7a0b5679d0d8cf1875/community/build/groovy/org/jetbrains/intellij/build/IntelliJCoreArtifactsBuilder.groovy?tab=source&line=18
//CORE_MODULES = [
//    "intellij.platform.util.rt",
//    "intellij.platform.util.classLoader",
//    "intellij.platform.util.text.matching",
//    "intellij.platform.util.base",
//    "intellij.platform.util.xmlDom",
//    "intellij.platform.util",
//    "intellij.platform.core",
//    "intellij.platform.core.impl",
//    "intellij.platform.extensions",
//    "intellij.java.psi",
//    "intellij.java.psi.impl",
//]
fun MavenPublication.configurePublication() {
    groupId = "com.github.xyzboom"
    artifactId = "intellij-core-for-psi-reference-extractor"
    version = "232.10300.40"
    pom {
        // 设置项目的元数据信息
        name.set("intellij-core-for-psi-reference-extractor")
        description.set("Intellij core module for com.github.xyzboom:psi-reference-extractor")
        url.set("https://github.com/XYZboom/psi-reference-extractor")
    }
}

publishing {
    publications {
        // 配置发布的库
        create<MavenPublication>("jar") {
            from(components["java"])
            artifacts {
                archives(tasks["jar"])
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
    api("com.jetbrains.intellij.platform:util-rt:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:util-class-loader:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:util-text-matching:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:util:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:util-base:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:util-xml-dom:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:core:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:core-impl:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.platform:extensions:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.java:java-psi:$intellijVersion") { isTransitive = false }
    api("com.jetbrains.intellij.java:java-psi-impl:$intellijVersion") { isTransitive = false }
}