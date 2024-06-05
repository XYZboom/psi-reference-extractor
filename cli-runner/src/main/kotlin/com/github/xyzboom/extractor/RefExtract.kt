package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.converter.ExporterConverter
import com.github.xyzboom.extractor.converter.GranularityConverter
import com.github.xyzboom.extractor.types.*
import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.psi.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.elements.KtLightMember
import org.jetbrains.kotlin.idea.references.KtDefaultAnnotationArgumentReference
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jgrapht.graph.DirectedWeightedMultigraph
import org.jgrapht.nio.DefaultAttribute
import picocli.CommandLine
import java.io.File
import java.io.FileWriter
import java.io.Writer
import kotlin.time.measureTime

@CommandLine.Command(name = "RefExtract")
class RefExtract : Runnable, KotlinJvmCompilerContext() {
    private val logger = KotlinLogging.logger {}

    @CommandLine.Parameters(index = "0", description = ["input directory"])
    lateinit var input: File

    @CommandLine.Option(names = ["-o", "--output"], description = ["output file prefix"], defaultValue = ".")
    lateinit var output: File

    @CommandLine.Option(
        names = ["-g", "--granularity"],
        split = ",", converter = [GranularityConverter::class], defaultValue = "file",
        description = ["granularity, choose in [file, class, member, expression]"]
    )
    lateinit var granularity: Array<TypedRefExtractor<PsiElement, GrammarOrRefEdge>>

    @CommandLine.Option(
        names = ["-f", "--format"],
        split = ",",
        converter = [ExporterConverter::class],
        defaultValue = "json"
    )
    lateinit var exporters: Array<ExporterProxy<PsiElement, GrammarOrRefEdge>>

    @CommandLine.Option(names = ["-eu", "--export-unknown"], description = ["export unknown references"])
    var exportUnknown: Boolean = false

    val elementGraph = DirectedWeightedMultigraph<PsiElement, GrammarOrRefEdge>(GrammarOrRefEdge::class.java)
    val dependElements = HashSet<PsiElement>()

    private inner class AddAllElementVisitor : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            elementGraph.addVertex(element)
            if (element.parent != null) {
                if (!elementGraph.containsVertex(element.parent)) {
                    elementGraph.addVertex(element.parent)
                }
                elementGraph.addEdge(element.parent, element, GrammarOrRefEdge(null))
            }
            super.visitElement(element)
        }
    }

    private inner class ReferenceRecorderVisitor : PsiRecursiveElementVisitor() {

        override fun visitElement(element: PsiElement) {
            elementGraph.addVertex(element)
            val reference = try {
                element.reference ?: kotlin.run {
                    return super.visitElement(element)
                }
            } catch (e: Exception) {
                logger.error { e.message }
                return super.visitElement(element)
            }
            val targets = try {
                reference.multiResolveToElement()
            } catch (e: Exception) {
                logger.error { e.message }
                return super.visitElement(element)
            }
            val referenceInfos = try {
                reference.referenceInfos
            } catch (e: Exception) {
                logger.error { e.message }
                return super.visitElement(element)
            }
            if (referenceInfos.size != targets.size) {
                return super.visitElement(element)
            }
            for ((i, target) in targets.withIndex()) {
                if (target is PsiPackage) continue
                val referenceInfo = referenceInfos[i]
                if (referenceInfo === ReferenceInfo.UNKNOWN && element !== target
                    && reference !is KtDefaultAnnotationArgumentReference
                ) {
                    reference.referenceInfos
                    logger.trace { "unknown reference info at source: ${element.posStr()}, target: ${target.posStr()}" }
                }
                if (referenceInfo != ReferenceInfo.UNKNOWN || exportUnknown) {
                    if (target !in elementGraph.vertexSet()) {
                        elementGraph.addVertex(target)
                        dependElements.add(target)
                    }
                    elementGraph.addEdge(element, target, GrammarOrRefEdge(referenceInfo))
                }
            }
            super.visitElement(element)
        }
    }

    private fun PsiElement.posStrWithoutFilePrefix(): String {
        val document = psiDocumentManager.getDocument(containingFile) ?: return "(Not in project)"
        textRange ?: return "(Not in project)"
        val startLine = document.getLineNumber(startOffset)
        val startCol = startOffset - document.getLineStartOffset(startLine)
        val containingPath = containingFile.virtualFile.path.replace("\\", "/")
        val inputPath = input.canonicalPath.replace("\\", "/")
        return "${containingPath.removePrefix(inputPath)}:${startLine + 1}:${startCol + 1}"
    }

    private fun PsiElement.getMyFqName(): String? = when {
        this is PsiFile -> virtualFile.path.removePrefix(input.canonicalPath)
        this is PsiQualifiedNamedElement && qualifiedName != null ->
            qualifiedName!!

        this is KtLightElement<*, *> && kotlinOrigin != null ->
            kotlinOrigin!!.formatToStr()

        this is PsiMember && containingClass != null ->
            containingClass!!.getMyFqName() + "." + name

        this is KtNamedDeclaration ->
            if (fqName != null) {
                fqName!!.asString()
            } else {
                when {
                    this is KtProperty ->
                        if (isLocal) {
                            // TODO
                            null
                        } else {
                            fqName?.asString()
                        }

                    this is KtConstructor<*> && getContainingClassOrObject().fqName != null
                            && getContainingClassOrObject().name != null -> {
                        val fqName = getContainingClassOrObject().fqName!!
                        val name = getContainingClassOrObject().name!!
                        fqName.asString() + "." + name
                    }

                    else -> {
                        val context = context
                        val contextParent = context?.parent
                        if (contextParent is KtNamedDeclaration) {
                            contextParent.fqName?.asString() + "." + name
                        } else {
                            null
                        }
                    }
                }
            }

        else -> null
    }

    private fun PsiElement.formatToStr(): String {
        val fqName = getMyFqName()
        return if (fqName == null) {
            posStrWithoutFilePrefix()
        } else {
            fqName + "|" + posStrWithoutFilePrefix()
        }
    }

    private fun checkArgs() {
        if (!input.canRead()) {
            error("${input.absolutePath} is not readable!")
        }
    }

    override fun run() {
        val allTime = measureTime {
            doRun()
        }
        logger.info { "all cost: $allTime" }
    }

    private fun doRun() {
        checkArgs()
        logger.info { "start init compiler env" }
        val initCompilerEnvTime = measureTime {
            initCompilerEnv(input.toPath())
        }
        logger.info { "init compiler cost: $initCompilerEnvTime" }
        logger.info { "start record all psi files" }
        if (exportUnknown) {
            logger.info { "running with export unknown references" }
        }
        val visitAllPsiFilesTime = measureTime {
            visitAllPsiFiles {
                if (it.containingDirectory.virtualFile.path.contains(".gradle")) {
                    return@visitAllPsiFiles
                }
                try {
                    it.accept(AddAllElementVisitor())
                } catch (e: Throwable) {
                    logger.error { "error when visit $it" }
                    logger.trace { e.stackTraceToString() }
                }
            }
        }
        logger.info { "record all psi files cost: $visitAllPsiFilesTime" }
        logger.info { "start analyze references" }
        val analyzeReferencesTime = measureTime {
            visitAllPsiFiles {
                it.accept(ReferenceRecorderVisitor())
            }
        }
        logger.info { "analyze references cost: $analyzeReferencesTime" }
        logDependencyInfo(elementGraph)
        logger.info { "start extract references" }
        val extractTime = measureTime {
            granularity.forEach { extractor ->
                val graph = extractor.doExtractor(elementGraph, allPsiFiles)
                exporters.forEach { exporter ->
                    exporter.setVertexAttributeProvider {
                        val labelText = if (it in dependElements) {
                            "${it.formatToStr()} (Not in project)"
                        } else it.formatToStr()
                        mutableMapOf("label" to DefaultAttribute.createAttribute(labelText))
                    }
                    exporter.setEdgeAttributeProvider {
                        mutableMapOf("label" to DefaultAttribute.createAttribute(it.referenceInfo.toString()))
                    }
                    val outputFile = if (output.isDirectory) {
                        File(output, "output-${extractor.name}.${exporter.name}")
                    } else {
                        File(output.parentFile, "${output.name}-${extractor.name}.${exporter.name}")
                    }
                    val writer: Writer = FileWriter(outputFile)
                    exporter.exportGraph(graph, writer)
                    writer.flush()
                }
            }
        }
        logger.info { "extract references cost: $extractTime" }
    }

    private fun logDependencyInfo(elementGraph: DirectedWeightedMultigraph<PsiElement, GrammarOrRefEdge>) {
        val countMap = hashMapOf<IReferenceType, Int>()
        for (edge in elementGraph.edgeSet()) {
            val info = edge.referenceInfo
            if (info != null) {
                if (countMap.contains(info.referenceType)) {
                    countMap[info.referenceType] = countMap[info.referenceType]!! + 1
                } else {
                    countMap[info.referenceType] = 1
                }
            }
        }
        for ((type, count) in countMap) {
            logger.trace { "$type: $count" }
        }
    }
}