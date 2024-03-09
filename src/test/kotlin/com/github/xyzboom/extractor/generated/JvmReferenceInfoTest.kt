// auto generated, do not manually edit!
package com.github.xyzboom.extractor.generated

import com.github.xyzboom.extractor.BaseJvmReferenceInfoTester
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path

class JvmReferenceInfoTest: BaseJvmReferenceInfoTester() {
    
    @Nested
    inner class ExpressionTest {
    
        @Nested
        inner class CallTest {
            @Test
            fun test_call0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call0"""))
                doValidate("""src\testData\jvm\expression\call\call0\result""")
            }
    
            @Test
            fun test_call1() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call1"""))
                doValidate("""src\testData\jvm\expression\call\call1\result""")
            }
    
            @Test
            fun test_call2() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call2"""))
                doValidate("""src\testData\jvm\expression\call\call2\result""")
            }
    
            @Test
            fun test_call3() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call3"""))
                doValidate("""src\testData\jvm\expression\call\call3\result""")
            }
    
            @Test
            fun test_call4() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call4"""))
                doValidate("""src\testData\jvm\expression\call\call4\result""")
            }
    
            @Test
            fun test_call5() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call5"""))
                doValidate("""src\testData\jvm\expression\call\call5\result""")
            }
    
            @Test
            fun test_call6() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call6"""))
                doValidate("""src\testData\jvm\expression\call\call6\result""")
            }
    
            @Test
            fun test_call7() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call7"""))
                doValidate("""src\testData\jvm\expression\call\call7\result""")
            }
    
            @Test
            fun test_call8() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call8"""))
                doValidate("""src\testData\jvm\expression\call\call8\result""")
            }
    
            @Test
            fun test_call9() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call9"""))
                doValidate("""src\testData\jvm\expression\call\call9\result""")
            }
    
            @Test
            fun test_callableProperty0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\callableProperty0"""))
                doValidate("""src\testData\jvm\expression\call\callableProperty0\result""")
            }
    
            @Test
            fun test_JavaCallKotlinToplevel0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\JavaCallKotlinToplevel0"""))
                doValidate("""src\testData\jvm\expression\call\JavaCallKotlinToplevel0\result""")
            }
    
    
        }
        
        @Nested
        inner class CreateTest {
            @Test
            fun test_create0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\create0"""))
                doValidate("""src\testData\jvm\expression\create\create0\result""")
            }
    
            @Test
            fun test_createWithNoArgConstructor() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\createWithNoArgConstructor"""))
                doValidate("""src\testData\jvm\expression\create\createWithNoArgConstructor\result""")
            }
    
    
        }
    
    }
        
    @Nested
    inner class ImportTest {
        @Test
        fun test_import0() {
            initCompilerEnv(Path.of("""src\testData\jvm\import\import0"""))
            doValidate("""src\testData\jvm\import\import0\result""")
        }
    
    
        @Nested
        inner class KotlinImportKotlinTopLevelsTest {
            @Test
            fun test_importFunction() {
                initCompilerEnv(Path.of("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importFunction"""))
                doValidate("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importFunction\result""")
            }
    
            @Test
            fun test_importProperty() {
                initCompilerEnv(Path.of("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importProperty"""))
                doValidate("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importProperty\result""")
            }
    
    
        }
    

    }
    
}