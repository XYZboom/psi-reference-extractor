package com.github.xyzboom.extractor

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlin.io.path.Path

object JvmTestGenerator {
    private val logger = KotlinLogging.logger {}

    private const val TEST_DATA_PATH = "src/testData/jvm/"
    private const val TEST_OUTPUT_PATH = "src/test/kotlin/com/github/xyzboom/extractor/generated/JvmTest.kt"
    private const val SCRIPTS_NAME = "predict.kts"
    private fun StringBuilder.appendEachTest(dir: File) {
        var testName = dir.path.replace(File.separator, "_").replace("src_testData", "test")
        if (testName.first().isDigit()) {
            testName = "_$testName"
        }
        append(
            """
            |@Test
            |fun $testName() {
            |    initCompilerEnv(Path.of(${"\"\"\""}${dir.path}${"\"\"\""}))
            |    doValidate(${"\"\"\""}${Path(dir.path, SCRIPTS_NAME)}${"\"\"\""})
            |}
        """.replaceIndentByMargin(" " * 4)
        )
        append(System.lineSeparator())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Start JvmTestGenerator" }
        val sb = StringBuilder()
        sb.append(
            """
            |// auto generated, do not manually edit!
            |package com.github.xyzboom.extractor.generated
            |
            |import com.github.xyzboom.extractor.JvmTester
            |import org.junit.jupiter.api.Test
            |import java.nio.file.Path
            |
            |class JvmTest: JvmTester() {
            |
        """.trimMargin()
        )
        val testPath = File(TEST_DATA_PATH)
        val fileTreeWalk = testPath.walkTopDown().filter { it.isDirectory && File(it, "predict.kts").exists() }
        fileTreeWalk.forEach {
            logger.info { it }
            sb.appendEachTest(it)
        }
        sb.append("}")
        val testOut = File(TEST_OUTPUT_PATH)
        if (testOut.exists()) {
            testOut.deleteRecursively()
        }
        testOut.writeText(sb.toString())
    }
}

private operator fun String.times(i: Int): String {
    val sb = StringBuilder()
    for (i1 in 0 until i) {
        sb.append(this)
    }
    return sb.toString()
}
