package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.converter.ExporterConverter
import com.github.xyzboom.extractor.converter.GranularityConverter
import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.idea.references.KtDefaultAnnotationArgumentReference
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
            if (element.parent != null && elementGraph.containsVertex(element.parent)) {
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
            val referenceInfos = reference.referenceInfos
            if (referenceInfos.size != targets.size) {
                return super.visitElement(element)
            }
            for ((i, target) in targets.withIndex()) {
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

    private fun PsiElement.formatToStr(): String {
        return when {
            this is PsiFile -> virtualFile.path
            dependElements.contains(this) -> "$this (Not in project)"
            else -> posStr()
        }
    }

    override fun run() {
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
                it.accept(AddAllElementVisitor())
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
}