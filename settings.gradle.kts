plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "psi-reference-extractor"

include(
    ":kt-references-analysis:analysis-api",
    ":kt-references-analysis:analysis-api-fe10",
    ":kt-references-analysis:analysis-api-impl-base",
    ":kt-references-analysis:analysis-internal-utils",
    ":kt-references-analysis:kt-references-fe10",
    ":kt-references-analysis:project-structure",
    "kt-references-analysis:extra-references",
    ":intellij-core",
)
include("test-framework")
include("cli-runner")
include("kotlin-compiler-context-utils")
