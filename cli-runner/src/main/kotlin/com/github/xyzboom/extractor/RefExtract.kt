package com.github.xyzboom.extractor

import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.idea.references.KtDefaultAnnotationArgumentReference
import org.jetbrains.kotlin.psi.KtProperty
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

    @CommandLine.Parameters(index = "1", description = ["output dot file prefix"])
    lateinit var output: File

    @CommandLine.Option(names = ["-f", "--format"], split = ",", converter = [ExporterConverter::class], defaultValue = "json")
    lateinit var exporters: Array<ExporterProxy<PsiElement, GrammarOrRefEdge>>

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
        fun visitKtProperty(source: KtProperty) {
        }

        override fun visitElement(element: PsiElement) {
            if (element is KtProperty) {
                visitKtProperty(element)
            }
            elementGraph.addVertex(element)
            val reference = element.reference ?: kotlin.run {
                super.visitElement(element)
                return
            }
            val targets = reference.multiResolveToElement()
            val referenceInfos = reference.referenceInfos
            require(referenceInfos.size == targets.size)
            for ((i, target) in targets.withIndex()) {
                val referenceInfo = referenceInfos[i]
                if (referenceInfo === ReferenceInfo.UNKNOWN && element !== target
                    && reference !is KtDefaultAnnotationArgumentReference
                ) {
                    reference.referenceInfos
                    logger.info { "unknown reference info at source: ${element.posStr()}, target: ${target.posStr()}" }
                }
                if (referenceInfo != ReferenceInfo.UNKNOWN) {
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

    override fun run() {
        logger.info { "start init compiler env" }
        val initCompilerEnvTime = measureTime {
            initCompilerEnv(input.toPath())
        }
        logger.info { "init compiler cost: $initCompilerEnvTime" }
        logger.info { "start record all psi files" }
        val visitAllPsiFilesTime = measureTime {
            visitAllPsiFiles {
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
            val collectEdgeFilter: (GrammarOrRefEdge) -> Boolean =
                { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) }
            val extractor = TypedRefExtractor<PsiElement, GrammarOrRefEdge>(
                { it is PsiFile }, { it.referenceInfo == null },
                collectEdgeFilter,
                GrammarOrRefEdge::class.java
            )
            val graph = extractor.doExtractor(elementGraph, allPsiFiles)
            exporters.forEach { exporter ->
                exporter.setVertexAttributeProvider {
                    val labelText = if (it in dependElements) {
                        "$it (Not in project)"
                    } else it.toString()
                    mutableMapOf("label" to DefaultAttribute.createAttribute(labelText))
                }
                exporter.setEdgeAttributeProvider {
                    mutableMapOf("label" to DefaultAttribute.createAttribute(it.referenceInfo.toString()))
                }
                val outputFile = if (output.isDirectory) {
                    File(output, "output.${exporter.name}")
                } else {
                    File(output.parentFile, "${output.name}.${exporter.name}")
                }
                val writer: Writer = FileWriter(outputFile)
                exporter.exportGraph(graph, writer)
                writer.flush()
            }

        }
        logger.info { "extract references cost: $extractTime" }
    }
}