package com.github.xyzboom.extractor

import com.intellij.lang.java.JavaLanguage
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.prevLeaf
import org.jetbrains.kotlin.analysis.api.descriptors.references.ReadWriteAccessCheckerDescriptorsImpl
import org.jetbrains.kotlin.analysis.api.impl.base.references.HLApiReferenceProviderService
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.KotlinReferenceProviderContributor
import org.jetbrains.kotlin.idea.references.ReadWriteAccessChecker
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.nextLeaf
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.references.fe10.base.DummyKtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.references.fe10.base.KtFe10KotlinReferenceProviderContributor
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.opentest4j.AssertionFailedError
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

private typealias CommentInFile = Pair<PsiComment, PsiFile>

open class BaseJvmReferenceInfoTester {
    private val disposer = Disposer.newDisposable()

    private val fileSystem: CoreLocalFileSystem = CoreLocalFileSystem()
    lateinit var project: MockProject
        private set
    lateinit var baseDir: VirtualFile
        private set
    lateinit var bindingContext: BindingContext
        private set

    lateinit var environment: KotlinCoreEnvironment
        private set

    lateinit var psiDocumentManager: PsiDocumentManager
        private set

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

    private fun PsiElement.posStr(): String {
        val document = psiDocumentManager.getDocument(containingFile)!!
        val startLine = document.getLineNumber(this.startOffset)
        val startCol = this.startOffset - document.getLineStartOffset(startLine)
        return "file:///${containingFile.virtualFile.path}:${startLine + 1}:${startCol + 1}"
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

    protected fun doValidate(scriptPath: String) {
        preparePsiElements()
        val resultText = File(scriptPath).readText()
        val lines = resultText.lines()
        if (resultText.isEmpty() || lines.all(String::isEmpty)) fail("test result file is empty!")
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

            val expectedInfo =
                engine.eval("createReferenceInfo(${result.split(" ").filter(String::isNotEmpty).joinToString()})")
            val actualInfo = source.reference?.referenceInfo
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
        environment.configuration.javaSourceRoots.mapNotNull(environment::findLocalFile).forEach {
            val file = PsiManager.getInstance(project).findFile(it)!!
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
            val sourceElement = sourceElementMap[key]
                ?: fail("no source named: $key found but target found between ${start.first.posStr()} and ${end.first.posStr()}.")
            checkReference(sourceElement, targetElement, key, start, end)
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
        val resolved = sourceElement.reference?.resolve()
        if (targetElement !== resolved) {
            if (resolved !is KtLightElement<*, *>) {
                fail(
                    "resolved reference must be target element!" +
                            " fail on source between ${sourceStartMap[key]!!.first.posStr()} and ${sourceEndMap[key]!!.first.posStr()}, " +
                            "end between ${start.first.posStr()} and ${end.first.posStr()}"
                )
            }
            assertEquals(targetElement, resolved.kotlinOrigin, "resolved reference must be target element")
        }
    }

    //<editor-fold desc="setup Env">

    fun initCompilerEnv(filePath: Path) {
        val jdkHome = File(System.getProperty("java.home")).toPath()
        val compilerEnvironmentContext = createCompilerConfiguration(
            listOf(filePath),
            listOf(filePath.absolutePathString()),
            jdkHome = jdkHome
        )
        environment = createKotlinCoreEnvironment(compilerEnvironmentContext)
        baseDir = CoreLocalVirtualFile(fileSystem, filePath.toFile(), filePath.isDirectory())
        bindingContext = generateBindingContext(
            environment,
            environment.getSourceFiles()
        )
        DummyKtFe10ReferenceResolutionHelper.bindingContext = bindingContext
        val application = ApplicationManager.getApplication()
        val resolutionHelper = DummyKtFe10ReferenceResolutionHelper
        if (application.getService(KtFe10ReferenceResolutionHelper::class.java) == null) {
            (application as MockApplication).registerService(
                KtFe10ReferenceResolutionHelper::class.java,
                resolutionHelper
            )
        }
        psiDocumentManager = PsiDocumentManager.getInstance(project)
    }

    private fun createKotlinCoreEnvironment(
        configuration: CompilerConfiguration = CompilerConfiguration(),
        printStream: PrintStream = System.err,
    ): KotlinCoreEnvironment {
        setIdeaIoUseFallback()
        configuration.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(printStream, MessageRenderer.PLAIN_FULL_PATHS, false)
        )
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "eligos")

        val environment = KotlinCoreEnvironment.createForProduction(
            disposer,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

        val projectCandidate = environment.project

        project = requireNotNull(projectCandidate as? MockProject) {
            "MockProject type expected, actual - ${projectCandidate.javaClass.simpleName}"
        }

        project.registerService(
            KotlinReferenceProviderContributor::class.java,
            KtFe10KotlinReferenceProviderContributor::class.java
        )

        project.registerService(KotlinReferenceProvidersService::class.java, HLApiReferenceProviderService(project))
        project.registerService(ReadWriteAccessChecker::class.java, ReadWriteAccessCheckerDescriptorsImpl())

        return environment
    }

    private fun createCompilerConfiguration(
        pathsToAnalyze: List<Path>,
        classpath: List<String>,
        languageVersion: LanguageVersion = LanguageVersion.KOTLIN_1_9,
        jvmTarget: JvmTarget = JvmTarget.JVM_1_8,
        jdkHome: Path?,
    ): CompilerConfiguration {
        val javaFiles = pathsToAnalyze.flatMap { path ->
            path.toFile().walk()
                .filter { it.isFile && it.extension.equals("java", true) }
                .toList()
        }
        val kotlinFiles = pathsToAnalyze.flatMap { path ->
            path.toFile().walk()
                .filter { it.isFile }
                .filter { it.extension.equals("kt", true) }
                .map { it.absolutePath }
                .toList()
        }

        val classpathFiles = classpath.map { File(it) }
        val languageVersionSettings: LanguageVersionSettings = languageVersion.let {
            LanguageVersionSettingsImpl(
                languageVersion = it,
                apiVersion = ApiVersion.createByLanguageVersion(it)
            )
        }

        return CompilerConfiguration().apply {
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings)
            put(JVMConfigurationKeys.JVM_TARGET, jvmTarget)
            addJavaSourceRoots(javaFiles)
            addKotlinSourceRoots(kotlinFiles)
            addJvmClasspathRoots(classpathFiles)
            addJvmClasspathRoot(kotlinStdLibPath())
            addJvmClasspathRoot(File("."))

            jdkHome?.let { put(JVMConfigurationKeys.JDK_HOME, it.toFile()) }
            configureJdkClasspathRoots()
        }
    }

    class MyMessageCollector : MessageCollector by MessageCollector.NONE {
        override fun report(
            severity: CompilerMessageSeverity,
            message: String,
            location: CompilerMessageSourceLocation?
        ) {
            print(message)
        }
    }

    private fun generateBindingContext(
        environment: KotlinCoreEnvironment,
        files: List<KtFile>
    ): BindingContext {

        val analyzer = AnalyzerWithCompilerReport(
            MyMessageCollector(),
            environment.configuration.languageVersionSettings,
            false,
        )
        analyzer.analyzeAndReport(files) {
            TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                environment.project,
                files,
                NoScopeRecordCliBindingTrace(),
                environment.configuration,
                environment::createPackagePartProvider,
                ::FileBasedDeclarationProviderFactory
            )
        }

        return analyzer.analysisResult.bindingContext
    }

    private fun kotlinStdLibPath(): File {
        return File(CharRange::class.java.protectionDomain.codeSource.location.path)
    }

    //</editor-fold>
    companion object {
        lateinit var engine: ScriptEngine
            private set

        @JvmStatic
        @BeforeAll
        fun setUpTestContext() {
            engine = ScriptEngineManager().getEngineByExtension("kts")!!
            val path = ReferenceInfo::class.java.protectionDomain.codeSource.location.path
            engine.eval("System.setProperty(\"kotlin.script.classpath\", \"$path\")")
            val bindings = engine.createBindings()
            bindings["kotlin"] = KotlinLanguage.INSTANCE
            bindings["java"] = JavaLanguage.INSTANCE
            bindings["info"] = ::ReferenceInfo
            engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE)
            engine.eval("import com.github.xyzboom.extractor.ReferenceInfo")
            engine.eval("import com.github.xyzboom.extractor.types.*")
            engine.eval("import com.intellij.lang.Language")
            engine.eval(
                """
                |fun createReferenceInfo(
                |   sourceLanguage: Language, sourceType: IReferenceSourceType,
                |   targetLanguage: Language?, targetType: IReferenceTargetType?
                |): ReferenceInfo {
                |   return ReferenceInfo(sourceLanguage, targetLanguage, sourceType, targetType)
                |}
            """.trimMargin()
            )
        }

    }

}