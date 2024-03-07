// auto generated, do not manually edit!
package com.github.xyzboom.extractor.generated

import com.github.xyzboom.extractor.JvmTester
import org.junit.jupiter.api.Test
import java.nio.file.Path

class JvmTest: JvmTester() {
    @Test
    fun test_jvm_expression_call_call5() {
        initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call5"""))
        doValidate("""src\testData\jvm\expression\call\call5\predict.kts""")
    }
}