package com.github.xyzboom.extractor

import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.util.prevLeaf
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.nextLeaf
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.BeforeEach
import org.opentest4j.AssertionFailedError
import java.io.File
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

private typealias CommentInFile = Pair<PsiComment, PsiFile>

open class BaseJvmReferenceInfoTester : KotlinJvmCompilerContext() {

    private val sourceElementMapScriptsName: String = "source"
    private val targetElementMapScriptsName: String = "target"
    private val sourcePrefix = "/*<source"
    private val targetPrefix = "/*<target"
    private val startSuffix = ">*/"
    private val endSuffix = "/>*/"
    private val defaultElementName = "default"
    private val nameAndResultSplit = ":"

    private val sourceStartMap = HashMap<String, CommentInFile>()
    private val sourceEndMap = HashMap<String, CommentInFile>()
    private val targetStartMap = HashMap<String, CommentInFile>()
    private val targetEndMap = HashMap<String, CommentInFile>()
    private val sourceElementMap = HashMap<String, PsiElement>()
    private val targetElementMap = HashMap<String, PsiElement>()

    @BeforeEach
    fun clearElements() {
        sourceStartMap.clear()
        sourceEndMap.clear()
        targetStartMap.clear()
        targetEndMap.clear()
        sourceElementMap.clear()
        targetElementMap.clear()
    }

    private fun visitLabeledComment(file: PsiFile, comment: PsiComment) {
        val commentText = comment.text
        when {
            commentText.startsWith(sourcePrefix) -> {
                when {
                    commentText.endsWith(endSuffix) -> {
                        val sourceName = commentText.removeSurrounding(sourcePrefix, endSuffix).defaultIfEmpty()
                        val psiComment = sourceEndMap.putIfAbsent(sourceName, comment to file)
                        if (psiComment != null) {
                            fail("source name: $sourceName already exists at: ${psiComment.first.posStr()}")
                        }
                    }

                    commentText.endsWith(startSuffix) -> {
                        val sourceName = commentText.removeSurrounding(sourcePrefix, startSuffix).defaultIfEmpty()
                        val psiComment = sourceStartMap.putIfAbsent(sourceName, comment to file)
                        if (psiComment != null) {
                            fail("source name: $sourceName already exists at: ${psiComment.first.posStr()}")
                        }
                    }
                }
            }

            commentText.startsWith(targetPrefix) -> {
                when {
                    commentText.endsWith(endSuffix) -> {
                        val sourceName = commentText.removeSurrounding(targetPrefix, endSuffix).defaultIfEmpty()
                        val psiComment = targetEndMap.putIfAbsent(sourceName, comment to file)
                        if (psiComment != null) {
                            fail("target name: $sourceName already exists at: ${psiComment.first.posStr()}")
                        }
                    }

                    commentText.endsWith(startSuffix) -> {
                        val sourceName = commentText.removeSurrounding(targetPrefix, startSuffix).defaultIfEmpty()
                        val psiComment = targetStartMap.putIfAbsent(sourceName, comment to file)
                        if (psiComment != null) {
                            fail("target name: $sourceName already exists at: ${psiComment.first.posStr()}")
                        }
                    }
                }
            }
        }
    }

    private fun String.defaultIfEmpty(): String = removePrefix(":").ifEmpty { defaultElementName }

    fun PsiElement.parentRangeIn(start: PsiElement, end: PsiElement, needReference: Boolean = false): PsiElement? {
        if (checkRange(start, end)
            && (!needReference || references.isNotEmpty())
        ) {
            if (parent is KtPrimaryConstructor && parent.checkRange(start, end)) {
                return parent
            }
            return this
        }
        if (parent == null || parent is PsiFile) return null
        return parent.parentRangeIn(start, end)
    }

    private fun PsiElement.checkRange(
        start: PsiElement,
        end: PsiElement
    ) = ((firstChild === start || prevLeaf() === start)
            && (lastChild === end || nextLeaf() === end))

    protected fun doValidate(scriptPath: String, extraScriptPath: String? = null) {
        preparePsiElements()
        val resultText = File(scriptPath).readText()
        val lines = resultText.lines()
        if (resultText.isEmpty() || lines.all(String::isEmpty)) fail("test result file is empty!")
        if (extraScriptPath != null) {
            val bindings = engine.createBindings()
            bindings[defaultElementName] = defaultElementName
            bindings.putAll(sourceElementMap.keys.associateWith { it })
            bindings[sourceElementMapScriptsName] = PsiMapWrapper(sourceElementMap)
            bindings[targetElementMapScriptsName] = PsiMapWrapper(targetElementMap)
            engine.eval("import com.github.xyzboom.extractor.PsiMapWrapper")
            engine.eval(File(extraScriptPath).readText(), bindings)
        }
        for (line in lines) {
            if (line.isEmpty()) {
                continue
            }
            val (name, result) = if (line.contains(nameAndResultSplit)) {
                val split = line.split(nameAndResultSplit)
                split[0] to split[1]
            } else defaultElementName to line
            val source = sourceElementMap[name] ?: fail("no source element $name found")
            val target = targetElementMap[name] ?: fail("no target element $name found")
            checkReference(source, target, name, targetStartMap[name]!!, targetEndMap[name]!!)

            val actualInfo = source.reference?.referenceInfos?.first()
            val expectedInfo =
                engine.eval("ReferenceInfo(${result.split(" ").filter(String::isNotEmpty).joinToString()})")
            if (
                expectedInfo
                != actualInfo
            ) {
                System.err.println("unexpected info!")
                System.err.println("source: ${source.posStr()}")
                System.err.println("target: ${target.posStr()}")
                throw AssertionFailedError("expected: <$expectedInfo> but was: <$actualInfo>")
            }
        }
    }

    private fun preparePsiElements() {
        visitAllPsiFiles { file ->
            if (file is PsiJavaFile) {
                file.accept(object : JavaRecursiveElementVisitor() {
                    override fun visitComment(comment: PsiComment) {
                        visitLabeledComment(file, comment)
                        super.visitComment(comment)
                    }
                })
            } else if (file is KtFile) {
                file.accept(object : KtTreeVisitorVoid() {
                    override fun visitComment(comment: PsiComment) {
                        visitLabeledComment(file, comment)
                        super.visitComment(comment)
                    }
                })
            }
        }
        require(sourceStartMap.isNotEmpty() && sourceEndMap.isNotEmpty()) {
            "no specified source element!"
        }
        require(targetStartMap.isNotEmpty() && targetEndMap.isNotEmpty()) {
            "no specified target element!"
        }
        require(sourceStartMap.keys == sourceEndMap.keys) {
            "source elements does not match! " +
                    if ((sourceEndMap.keys - sourceStartMap.keys).isNotEmpty()) {
                        "These names has end label($sourcePrefix$endSuffix) but not has start label($sourcePrefix$startSuffix):" +
                                "${sourceEndMap.keys - sourceStartMap.keys}"
                    } else {
                        ""
                    } +
                    if ((sourceStartMap.keys - sourceEndMap.keys).isNotEmpty()) {
                        "These names has start label($sourcePrefix$startSuffix) but not has end label($sourcePrefix$endSuffix):" +
                                "${sourceStartMap.keys - sourceEndMap.keys}"
                    } else {
                        ""
                    }
        }
        require(targetStartMap.keys == targetEndMap.keys) {
            "target elements does not match! " +
                    if ((targetEndMap.keys - targetStartMap.keys).isNotEmpty()) {
                        "These names has end label($targetPrefix$endSuffix) but not has start label($targetPrefix$startSuffix):" +
                                "${targetEndMap.keys - targetStartMap.keys}\n"
                    } else {
                        ""
                    } +
                    if ((targetStartMap.keys - targetEndMap.keys).isNotEmpty()) {
                        "These names has start label($sourcePrefix$startSuffix) but not has end label($sourcePrefix$endSuffix):" +
                                "${targetStartMap.keys - targetEndMap.keys}\n"
                    } else {
                        ""
                    }
        }
        for ((key, start) in sourceStartMap) {
            val end = sourceEndMap[key]
                ?: fail("no source end element named $key, start element is at: ${start.first.posStr()}")
            require(start.second === end.second) {
                "The start element at: ${start.first.posStr()} must be in the same file as the end element at: ${end.first.posStr()}"
            }
            val sourceElement = start.second.findElementAt(start.first.endOffset)
                ?.parentRangeIn(start.first, end.first, true)
                ?: fail("Could not found element that has reference between ${start.first.posStr()} and ${end.first.posStr()}.")
            sourceElementMap[key] = sourceElement
        }
        for ((key, start) in targetStartMap) {
            val end = targetEndMap[key]
                ?: fail("no target end element named $key, start element is at: ${start.first.posStr()}")
            require(start.second === end.second) {
                "The start element at: ${start.first.posStr()} must be in the same file as the end element at: ${end.first.posStr()}"
            }
            val targetElement = start.second.findElementAt(start.first.endOffset)
                ?.parentRangeIn(start.first, end.first)
                ?: fail("Could not found element between ${start.first.posStr()} and ${end.first.posStr()}.")
            sourceElementMap[key]
                ?: fail("no source named: $key found but target found between ${start.first.posStr()} and ${end.first.posStr()}.")
            // reference check must do after extra scripts done.
            // so there is no reference check
            targetElementMap[key] = targetElement
        }
    }

    private fun checkReference(
        sourceElement: PsiElement,
        targetElement: PsiElement,
        key: String,
        start: CommentInFile,
        end: CommentInFile
    ) {
        val resolved = sourceElement.references.flatMap {
            if (it is PsiPolyVariantReference) it.multiResolve(false).map { it1 -> it1.element }
            else listOf(it.resolve())
        }.filterNotNull()
        if (resolved.none { it.isEquivalentTo(targetElement) }) {
            val failMessage = "resolved reference must be target element!" +
                    " fail on source between ${sourceStartMap[key]!!.first.posStr()} and ${sourceEndMap[key]!!.first.posStr()}, " +
                    "end between ${start.first.posStr()} and ${end.first.posStr()}"
            if (resolved.none { it is KtLightElement<*, *> }) {
                fail(failMessage)
            }
            if (!targetElement.isEquivalentTo((resolved.first { it is KtLightElement<*, *> } as KtLightElement<*, *>).kotlinOrigin)) {
                fail(failMessage)
            }
        }
    }

    //<editor-fold desc="setup Env">

    //</editor-fold>
    companion object {
        lateinit var engine: ScriptEngine
            private set

        @JvmStatic
        @BeforeAll
        fun setUpTestContext() {
            engine = ScriptEngineManager().getEngineByExtension("kts")!!
            val path = ClassLoader.getSystemClassLoader().resources("").map { it.path }.toList().joinToString(",")
            engine.eval("System.setProperty(\"kotlin.script.classpath\", \"$path\")")
            val bindings = engine.createBindings()
            bindings["kotlin"] = KotlinLanguage.INSTANCE
            bindings["java"] = JavaLanguage.INSTANCE
            bindings["info"] = ::ReferenceInfo
            engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE)
            engine.eval("import com.github.xyzboom.extractor.ReferenceInfo")
            engine.eval("import com.github.xyzboom.extractor.types.*")
            engine.eval("import com.intellij.lang.Language")
        }

    }

}