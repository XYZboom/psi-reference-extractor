package com.github.xyzboom.ktcutils

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import com.intellij.psi.*
import org.jetbrains.kotlin.analysis.api.descriptors.references.ReadWriteAccessCheckerDescriptorsImpl
import org.jetbrains.kotlin.analysis.api.impl.base.references.HLApiReferenceProviderService
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
import org.jetbrains.kotlin.idea.references.KotlinReferenceProviderContributor
import org.jetbrains.kotlin.idea.references.ReadWriteAccessChecker
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.references.fe10.base.DummyKtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.references.fe10.base.KtFe10KotlinReferenceProviderContributor
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

open class KotlinJvmCompilerContext {
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

    val allPsiFiles: List<PsiFile>
        get() = environment.configuration.javaSourceRoots.mapNotNull(environment::findLocalFile).map {
            PsiManager.getInstance(project).findFile(it)!!
        }

    fun visitAllPsiFiles(visitor: (PsiFile) -> Unit) {
        environment.configuration.javaSourceRoots.mapNotNull(environment::findLocalFile).forEach {
            val file = PsiManager.getInstance(project).findFile(it)!!
                visitor(file)
        }
    }

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
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "psi-reference-extractor")

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

    protected open fun PsiElement.posStr(): String {
        val document = psiDocumentManager.getDocument(containingFile) ?: return "(Not in project)"
        val startLine = document.getLineNumber(startOffset)
        val startCol = startOffset - document.getLineStartOffset(startLine)
        return "file:///${containingFile.virtualFile.path}:${startLine + 1}:${startCol + 1}"
    }

    class MyMessageCollector : MessageCollector by MessageCollector.NONE {
        override fun report(
            severity: CompilerMessageSeverity,
            message: String,
            location: CompilerMessageSourceLocation?
        ) {
//            print(message)
        }
    }
}