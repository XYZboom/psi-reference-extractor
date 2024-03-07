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
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.nextLeaf
import org.jetbrains.kotlin.references.fe10.base.DummyKtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.references.fe10.base.KtFe10KotlinReferenceProviderContributor
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

open class JvmTester {
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

    val sourceStartText = "/*<source>*/"
    val sourceEndText = "/*<source/>*/"
    val targetStartText = "/*<target>*/"
    val targetEndText = "/*<target/>*/"

    var sourceStart: PsiComment? = null
    var sourceEnd: PsiComment? = null
    var targetStart: PsiComment? = null
    var targetEnd: PsiComment? = null
    var sourceFile: PsiFile? = null
    var targetFile: PsiFile? = null

    private fun visitLabeledComment(file: PsiFile, comment: PsiComment) {
        when (comment.text) {
            sourceStartText -> {
                sourceStart = comment
                sourceFile = file
            }

            sourceEndText -> {
                sourceEnd = comment
                require(file == sourceFile) {
                    "source element comment should be in the same file!"
                }
            }

            targetStartText -> {
                targetStart = comment
                targetFile = file
            }

            targetEndText -> {
                targetEnd = comment
                require(file == targetFile) {
                    "target element comment should be in the same file!"
                }
            }
        }
    }

    fun PsiElement.parentRangeIn(start: PsiElement, end: PsiElement, needReference: Boolean = false): PsiElement? {
        if ((firstChild === start || prevLeaf() === start)
            && (lastChild === end || nextLeaf() === end)
            && (!needReference || references.isNotEmpty())
        ) return this
        if (parent == null || parent is PsiFile) return null
        return parent.parentRangeIn(start, end)
    }

    protected fun doValidate(scriptPath: String) {
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
        require(
            sourceStart != null && sourceEnd != null && targetStart != null && targetEnd != null
                    && sourceFile != null && targetFile != null
        ) { "no source or target in test files!" }
        val sourceElement =
            sourceFile!!.findElementAt(sourceStart!!.endOffset)?.parentRangeIn(sourceStart!!, sourceEnd!!, true)
                ?: fail("could not find source element")
        val targetElement =
            targetFile!!.findElementAt(targetStart!!.endOffset)?.parentRangeIn(targetStart!!, targetEnd!!)
                ?: fail("could not find target element")
        val resolved = sourceElement.reference?.resolve()
        if (targetElement !== resolved) {
            if (resolved !is KtLightElement<*, *>) {
                fail("resolved reference must be target element")
            }
            assertEquals(targetElement, resolved.kotlinOrigin, "resolved reference must be target element")
        }
        val resultText = File(scriptPath).readText()
        assertEquals(
            engine.eval("createReferenceInfo(${resultText.split(" ").joinToString()})"),
            sourceElement.reference?.referenceInfo
        )
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