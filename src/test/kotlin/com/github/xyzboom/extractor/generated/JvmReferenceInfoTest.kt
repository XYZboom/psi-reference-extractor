// auto generated, do not manually edit!
package com.github.xyzboom.extractor.generated

import com.github.xyzboom.extractor.BaseJvmReferenceInfoTester
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path

class JvmReferenceInfoTest : BaseJvmReferenceInfoTester() {
    
    @Nested
    inner class AnnotationTest {
        @Test
        fun test_annotation0() {
            initCompilerEnv(Path.of("""src\testData\jvm\annotation\annotation0"""))
            doValidate(
                """src\testData\jvm\annotation\annotation0\result""",
                """src\testData\jvm\annotation\annotation0\extra"""
            )
        }
    
    
    }
        
    @Nested
    inner class ExpressionTest {
    
        @Nested
        inner class AccessTest {
            @Test
            fun test_kotlinAccessJavaMethod0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\kotlinAccessJavaMethod0"""))
                doValidate(
                    """src\testData\jvm\expression\access\kotlinAccessJavaMethod0\result""",
                    null
                )
            }
    
    
        }
        
        @Nested
        inner class CallTest {
            @Test
            fun test_call0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call0"""))
                doValidate(
                    """src\testData\jvm\expression\call\call0\result""",
                    null
                )
            }
    
            @Test
            fun test_call1() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call1"""))
                doValidate(
                    """src\testData\jvm\expression\call\call1\result""",
                    null
                )
            }
    
            @Test
            fun test_call2() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call2"""))
                doValidate(
                    """src\testData\jvm\expression\call\call2\result""",
                    null
                )
            }
    
            @Test
            fun test_call3() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call3"""))
                doValidate(
                    """src\testData\jvm\expression\call\call3\result""",
                    null
                )
            }
    
            @Test
            fun test_call4() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call4"""))
                doValidate(
                    """src\testData\jvm\expression\call\call4\result""",
                    null
                )
            }
    
            @Test
            fun test_call5() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call5"""))
                doValidate(
                    """src\testData\jvm\expression\call\call5\result""",
                    null
                )
            }
    
            @Test
            fun test_call6() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call6"""))
                doValidate(
                    """src\testData\jvm\expression\call\call6\result""",
                    null
                )
            }
    
            @Test
            fun test_call7() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call7"""))
                doValidate(
                    """src\testData\jvm\expression\call\call7\result""",
                    null
                )
            }
    
            @Test
            fun test_call8() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call8"""))
                doValidate(
                    """src\testData\jvm\expression\call\call8\result""",
                    null
                )
            }
    
            @Test
            fun test_call9() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\call9"""))
                doValidate(
                    """src\testData\jvm\expression\call\call9\result""",
                    null
                )
            }
    
            @Test
            fun test_callableProperty0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\callableProperty0"""))
                doValidate(
                    """src\testData\jvm\expression\call\callableProperty0\result""",
                    null
                )
            }
    
            @Test
            fun test_JavaCallKotlinToplevel0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\call\JavaCallKotlinToplevel0"""))
                doValidate(
                    """src\testData\jvm\expression\call\JavaCallKotlinToplevel0\result""",
                    null
                )
            }
    
    
        }
        
        @Nested
        inner class CreateTest {
            @Test
            fun test_create0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\create0"""))
                doValidate(
                    """src\testData\jvm\expression\create\create0\result""",
                    null
                )
            }
    
            @Test
            fun test_createWithNoArgConstructor() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\createWithNoArgConstructor"""))
                doValidate(
                    """src\testData\jvm\expression\create\createWithNoArgConstructor\result""",
                    null
                )
            }
    
            @Test
            fun test_javaCreate0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\javaCreate0"""))
                doValidate(
                    """src\testData\jvm\expression\create\javaCreate0\result""",
                    null
                )
            }
    
            @Test
            fun test_javaCreate1() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\create\javaCreate1"""))
                doValidate(
                    """src\testData\jvm\expression\create\javaCreate1\result""",
                    null
                )
            }
    
    
        }
        
        @Nested
        inner class DestructureTest {
            @Test
            fun test_destructure0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\destructure\destructure0"""))
                doValidate(
                    """src\testData\jvm\expression\destructure\destructure0\result""",
                    null
                )
            }
    
    
        }
        
        @Nested
        inner class OperatorOverloadTest {
            @Test
            fun test_operatorOverload0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\operatorOverload\operatorOverload0"""))
                doValidate(
                    """src\testData\jvm\expression\operatorOverload\operatorOverload0\result""",
                    null
                )
            }
    
    
        }
    
    }
        
    @Nested
    inner class ExtensionTest {
    
        @Nested
        inner class FunctionTest {
            @Test
            fun test_function0() {
                initCompilerEnv(Path.of("""src\testData\jvm\extension\function\function0"""))
                doValidate(
                    """src\testData\jvm\extension\function\function0\result""",
                    null
                )
            }
    
    
        }
    
    }
        
    @Nested
    inner class ImportTest {
        @Test
        fun test_import0() {
            initCompilerEnv(Path.of("""src\testData\jvm\import\import0"""))
            doValidate(
                """src\testData\jvm\import\import0\result""",
                null
            )
        }
    
    
        @Nested
        inner class KotlinImportKotlinTopLevelsTest {
            @Test
            fun test_importFunction() {
                initCompilerEnv(Path.of("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importFunction"""))
                doValidate(
                    """src\testData\jvm\import\kotlinImportKotlinTopLevels\importFunction\result""",
                    null
                )
            }
    
            @Test
            fun test_importProperty() {
                initCompilerEnv(Path.of("""src\testData\jvm\import\kotlinImportKotlinTopLevels\importProperty"""))
                doValidate(
                    """src\testData\jvm\import\kotlinImportKotlinTopLevels\importProperty\result""",
                    null
                )
            }
    
    
        }
    
    }
        
    @Nested
    inner class InheritTest {
        @Test
        fun test_inherit0() {
            initCompilerEnv(Path.of("""src\testData\jvm\inherit\inherit0"""))
            doValidate(
                """src\testData\jvm\inherit\inherit0\result""",
                null
            )
        }
    
        @Test
        fun test_inherit1() {
            initCompilerEnv(Path.of("""src\testData\jvm\inherit\inherit1"""))
            doValidate(
                """src\testData\jvm\inherit\inherit1\result""",
                null
            )
        }
    
    
    }
        
    @Nested
    inner class ParameterTest {
        @Test
        fun test_constructorParameter0() {
            initCompilerEnv(Path.of("""src\testData\jvm\parameter\constructorParameter0"""))
            doValidate(
                """src\testData\jvm\parameter\constructorParameter0\result""",
                null
            )
        }
    
        @Test
        fun test_constructorParameter1() {
            initCompilerEnv(Path.of("""src\testData\jvm\parameter\constructorParameter1"""))
            doValidate(
                """src\testData\jvm\parameter\constructorParameter1\result""",
                null
            )
        }
    
        @Test
        fun test_methodParameter0() {
            initCompilerEnv(Path.of("""src\testData\jvm\parameter\methodParameter0"""))
            doValidate(
                """src\testData\jvm\parameter\methodParameter0\result""",
                null
            )
        }
    
    
    }
        
    @Nested
    inner class PropertyTest {
        @Test
        fun test_javaFieldAsProperty() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\javaFieldAsProperty"""))
            doValidate(
                """src\testData\jvm\property\javaFieldAsProperty\result""",
                null
            )
        }
    
        @Test
        fun test_kotlinProperty() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\kotlinProperty"""))
            doValidate(
                """src\testData\jvm\property\kotlinProperty\result""",
                null
            )
        }
    
        @Test
        fun test_kotlinProperty1() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\kotlinProperty1"""))
            doValidate(
                """src\testData\jvm\property\kotlinProperty1\result""",
                null
            )
        }
    
        @Test
        fun test_propertyDelegate0() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\propertyDelegate0"""))
            doValidate(
                """src\testData\jvm\property\propertyDelegate0\result""",
                null
            )
        }
    
    
    }
        
    @Nested
    inner class ReturnTest {
        @Test
        fun test_methodReturn0() {
            initCompilerEnv(Path.of("""src\testData\jvm\return\methodReturn0"""))
            doValidate(
                """src\testData\jvm\return\methodReturn0\result""",
                null
            )
        }
    
        @Test
        fun test_methodReturn1() {
            initCompilerEnv(Path.of("""src\testData\jvm\return\methodReturn1"""))
            doValidate(
                """src\testData\jvm\return\methodReturn1\result""",
                null
            )
        }
    
    
    }
    
}