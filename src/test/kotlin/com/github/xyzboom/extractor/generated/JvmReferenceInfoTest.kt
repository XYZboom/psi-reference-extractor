// auto generated, do not manually edit!
package com.github.xyzboom.extractor.generated

import com.github.xyzboom.extractor.BaseJvmReferenceInfoTester
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
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
    
        @Test
        fun test_annotationInJavaClass() {
            initCompilerEnv(Path.of("""src\testData\jvm\annotation\annotationInJavaClass"""))
            doValidate(
                """src\testData\jvm\annotation\annotationInJavaClass\result""",
                """src\testData\jvm\annotation\annotationInJavaClass\extra"""
            )
        }
    
        @Test
        fun test_annotationInJavaField() {
            initCompilerEnv(Path.of("""src\testData\jvm\annotation\annotationInJavaField"""))
            doValidate(
                """src\testData\jvm\annotation\annotationInJavaField\result""",
                """src\testData\jvm\annotation\annotationInJavaField\extra"""
            )
        }
    
        @Test
        fun test_annotationInJavaMethod() {
            initCompilerEnv(Path.of("""src\testData\jvm\annotation\annotationInJavaMethod"""))
            doValidate(
                """src\testData\jvm\annotation\annotationInJavaMethod\result""",
                """src\testData\jvm\annotation\annotationInJavaMethod\extra"""
            )
        }
    
    
    }
        
    @Nested
    inner class DelegateTest {
        @Test
        fun test_delegate0() {
            initCompilerEnv(Path.of("""src\testData\jvm\delegate\delegate0"""))
            doValidate(
                """src\testData\jvm\delegate\delegate0\result""",
                null
            )
        }
    
    
    }
        
    @Nested
    inner class ExpressionTest {
    
        @Nested
        inner class AccessTest {
            @Test
            fun test_javaAccessJavaClass() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\javaAccessJavaClass"""))
                doValidate(
                    """src\testData\jvm\expression\access\javaAccessJavaClass\result""",
                    null
                )
            }
    
            @Test
            fun test_javaAccessJavaField0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\javaAccessJavaField0"""))
                doValidate(
                    """src\testData\jvm\expression\access\javaAccessJavaField0\result""",
                    null
                )
            }
    
            @Test
            fun test_javaAccessJavaField1() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\javaAccessJavaField1"""))
                doValidate(
                    """src\testData\jvm\expression\access\javaAccessJavaField1\result""",
                    null
                )
            }
    
            @Test
            fun test_javaAccessKotlinClass() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\javaAccessKotlinClass"""))
                doValidate(
                    """src\testData\jvm\expression\access\javaAccessKotlinClass\result""",
                    null
                )
            }
    
            @Test
            fun test_kotlinAccessJavaMethod0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\kotlinAccessJavaMethod0"""))
                doValidate(
                    """src\testData\jvm\expression\access\kotlinAccessJavaMethod0\result""",
                    null
                )
            }
    
            @Test
            fun test_kotlinAccessKotlinProperty0() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\access\kotlinAccessKotlinProperty0"""))
                doValidate(
                    """src\testData\jvm\expression\access\kotlinAccessKotlinProperty0\result""",
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
    
    
            @Nested
            inner class CallUnionTypeFuncTest {
                @Test
                fun test_callUnionTypeFunc0() {
                    initCompilerEnv(Path.of("""src\testData\jvm\expression\call\callUnionTypeFunc\callUnionTypeFunc0"""))
                    doValidate(
                        """src\testData\jvm\expression\call\callUnionTypeFunc\callUnionTypeFunc0\result""",
                        null
                    )
                }
    
    
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
    
            @Test
        @Disabled
            fun test_destructure1() {
                initCompilerEnv(Path.of("""src\testData\jvm\expression\destructure\destructure1"""))
                doValidate(
                    """src\testData\jvm\expression\destructure\destructure1\result""",
                    null
                )
            }
    
    
        }
        
        @Nested
        inner class OperatorOverloadTest {
    
            @Nested
            inner class OperatorInvokeTest {
                @Test
                fun test_invoke0() {
                    initCompilerEnv(Path.of("""src\testData\jvm\expression\operatorOverload\operatorInvoke\invoke0"""))
                    doValidate(
                        """src\testData\jvm\expression\operatorOverload\operatorInvoke\invoke0\result""",
                        null
                    )
                }
    
    
            }
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
    inner class GenericTest {
        @Test
        fun test_generic0() {
            initCompilerEnv(Path.of("""src\testData\jvm\generic\generic0"""))
            doValidate(
                """src\testData\jvm\generic\generic0\result""",
                null
            )
        }
    
        @Test
        fun test_generic3() {
            initCompilerEnv(Path.of("""src\testData\jvm\generic\generic3"""))
            doValidate(
                """src\testData\jvm\generic\generic3\result""",
                null
            )
        }
    
        @Test
        fun test_generic4() {
            initCompilerEnv(Path.of("""src\testData\jvm\generic\generic4"""))
            doValidate(
                """src\testData\jvm\generic\generic4\result""",
                null
            )
        }
    
        @Test
        fun test_generic5() {
            initCompilerEnv(Path.of("""src\testData\jvm\generic\generic5"""))
            doValidate(
                """src\testData\jvm\generic\generic5\result""",
                null
            )
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
    
        @Test
        fun test_import1() {
            initCompilerEnv(Path.of("""src\testData\jvm\import\import1"""))
            doValidate(
                """src\testData\jvm\import\import1\result""",
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
    
        @Nested
        inner class AnonymousClassTest {
            @Test
            fun test_anonymousClass0() {
                initCompilerEnv(Path.of("""src\testData\jvm\inherit\anonymousClass\anonymousClass0"""))
                doValidate(
                    """src\testData\jvm\inherit\anonymousClass\anonymousClass0\result""",
                    null
                )
            }
    
            @Test
            fun test_anonymousClass1() {
                initCompilerEnv(Path.of("""src\testData\jvm\inherit\anonymousClass\anonymousClass1"""))
                doValidate(
                    """src\testData\jvm\inherit\anonymousClass\anonymousClass1\result""",
                    null
                )
            }
    
    
        }
            @Test
        fun test_constructorDelegation0() {
            initCompilerEnv(Path.of("""src\testData\jvm\inherit\constructorDelegation0"""))
            doValidate(
                """src\testData\jvm\inherit\constructorDelegation0\result""",
                null
            )
        }
    
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
    
        @Test
        fun test_inheritJ_J() {
            initCompilerEnv(Path.of("""src\testData\jvm\inherit\inheritJ_J"""))
            doValidate(
                """src\testData\jvm\inherit\inheritJ_J\result""",
                null
            )
        }
    
        @Test
        fun test_inheritJ_K() {
            initCompilerEnv(Path.of("""src\testData\jvm\inherit\inheritJ_K"""))
            doValidate(
                """src\testData\jvm\inherit\inheritJ_K\result""",
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
        fun test_constructorParameter2() {
            initCompilerEnv(Path.of("""src\testData\jvm\parameter\constructorParameter2"""))
            doValidate(
                """src\testData\jvm\parameter\constructorParameter2\result""",
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
    
        @Test
        fun test_methodParameter1() {
            initCompilerEnv(Path.of("""src\testData\jvm\parameter\methodParameter1"""))
            doValidate(
                """src\testData\jvm\parameter\methodParameter1\result""",
                null
            )
        }
    
    
    }
        
    @Nested
    inner class PropertyTest {
        @Test
        fun test_fieldTyped() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\fieldTyped"""))
            doValidate(
                """src\testData\jvm\property\fieldTyped\result""",
                null
            )
        }
    
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
        fun test_kotlinPropertyAsJavaMethod() {
            initCompilerEnv(Path.of("""src\testData\jvm\property\kotlinPropertyAsJavaMethod"""))
            doValidate(
                """src\testData\jvm\property\kotlinPropertyAsJavaMethod\result""",
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
    
        @Test
        fun test_methodReturn2() {
            initCompilerEnv(Path.of("""src\testData\jvm\return\methodReturn2"""))
            doValidate(
                """src\testData\jvm\return\methodReturn2\result""",
                null
            )
        }
    
    
    }
    
}