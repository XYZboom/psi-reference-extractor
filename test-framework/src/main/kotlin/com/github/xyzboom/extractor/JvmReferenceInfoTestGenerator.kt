package com.github.xyzboom.extractor

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.system.exitProcess

object JvmReferenceInfoTestGenerator {
    private val logger = KotlinLogging.logger {}

    private const val TEST_DATA_PATH = "src/testData/jvm/"
    private const val TEST_OUTPUT_PATH = "src/test/kotlin/com/github/xyzboom/extractor/generated/JvmReferenceInfoTest.kt"
    private const val RESULT_FILE_NAME = "result"
    private const val EXTRA_SCRIPTS_FILE_NAME = "extra"
    private const val MAIN_TEST_CLASS_NAME = "JvmReferenceInfoTest"
    private val sb = StringBuilder()
    private val fileStateStack = Stack<Pair<File, StringBuilder>>()

    /**
     * Invoke when enter a test root directory.
     *
     * A test root always contains a text file named "result".
     *
     * @param dir test root directory
     */
    private fun enterTestRootDir(dir: File) {
        val testNameDir = dir.path.split(File.separator).last()
        val testName = "test_$testNameDir"
        val (file, sb) = fileStateStack.peek()
        require(File(file, testNameDir) == dir)
        val extraScriptsFile = File(dir, EXTRA_SCRIPTS_FILE_NAME)
        sb.append(
            """
            |@Test
            |fun $testName() {
            |    initCompilerEnv(Path.of(${"\"\"\""}${dir.path}${"\"\"\""}))
            |    doValidate(
            |        ${"\"\"\""}${Path(dir.path, RESULT_FILE_NAME)}${"\"\"\""},
            |        ${if (extraScriptsFile.exists()) "${"\"\"\""}${extraScriptsFile.path}${"\"\"\""}" else "null"}
            |    )
            |}
            |
        """.replaceIndentByMargin(" " * 4)
        )
        sb.append(System.lineSeparator())
    }

    /**
     * invoke when enter sub-test-directory in order to generate test classes.
     *
     * For example, test directory in [TEST_DATA_PATH] is "import/myImportTest1/",
     * the class "Import" inside [MAIN_TEST_CLASS_NAME] while be generated.
     */
    private fun enterSubTestDir(dir: File) {
        fileStateStack.push(dir to StringBuilder())
    }

    private fun CharSequence.prependIndent(indent: String = "    "): String =
        lineSequence()
            .map {
                when {
                    it.isBlank() -> {
                        when {
                            it.length < indent.length -> indent
                            else -> it
                        }
                    }
                    else -> indent + it
                }
            }
            .joinToString("\n")

    private fun leaveSubTestDir(dir: File) {
        val (_, sb) = fileStateStack.pop()
        if (sb.isEmpty()) return
        val (_, parentSb) = fileStateStack.peek()
        val className = dir.path.split(File.separator).last()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val sbNow = StringBuilder()
        sbNow.append(System.lineSeparator())
        sbNow.append("@Nested")
        sbNow.append(System.lineSeparator())
        sbNow.append("""inner class ${className}Test {""")
        sbNow.append(System.lineSeparator())
        sbNow.append(sb)
        sbNow.append(System.lineSeparator())
        sbNow.append("}")
        sbNow.append(System.lineSeparator())
        parentSb.append(sbNow.prependIndent())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Start JvmTestGenerator" }
        if (sb.isNotEmpty()) {
            logger.error { "Test Class Has Already Generated!" }
            exitProcess(-1)
        }
        sb.append(
            """
            |// auto generated, do not manually edit!
            |package com.github.xyzboom.extractor.generated
            |
            |import com.github.xyzboom.extractor.BaseJvmReferenceInfoTester
            |import org.junit.jupiter.api.Nested
            |import org.junit.jupiter.api.Test
            |import java.nio.file.Path
            |
            |class $MAIN_TEST_CLASS_NAME : BaseJvmReferenceInfoTester() {
            |
        """.trimMargin()
        )
        val testPath = File(TEST_DATA_PATH)
        fileStateStack.push(testPath to sb)
        val fileTreeWalk = testPath.walkTopDown()
            .onEnter {
                if (it == testPath) return@onEnter true
                if (it.isDirectory && File(it, RESULT_FILE_NAME).exists()) {
                    enterTestRootDir(it)
                    return@onEnter true
                }
                enterSubTestDir(it)
                return@onEnter true
            }.onLeave {
                if (it == testPath) return@onLeave
                if (it.isDirectory && File(it, RESULT_FILE_NAME).exists()) {
                    return@onLeave
                }
                leaveSubTestDir(it)
                return@onLeave
            }
        fileTreeWalk.forEach { _ -> }
        sb.append(System.lineSeparator())
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
