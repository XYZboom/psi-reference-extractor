package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.ReferenceInfo.Companion.UNKNOWN
import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.idea.references.KtDefaultAnnotationArgumentReference
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import java.io.FileWriter
import java.io.Writer
import kotlin.system.exitProcess
import kotlin.time.measureTime


@Command(name = "RefExtract")
class RefExtract : Runnable, KotlinJvmCompilerContext() {
    private val logger = KotlinLogging.logger {}

    @Parameters(index = "0", description = ["input directory"])
    lateinit var input: File

    @Parameters(index = "1", description = ["output dot file"])
    lateinit var output: File

    val elementGraph = DefaultDirectedGraph<PsiElement, GrammarOrRefEdge>(GrammarOrRefEdge::class.java)

    private inner class AddAllElementVisitor : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            elementGraph.addVertex(element)
            if (element.parent != null && elementGraph.containsVertex(element.parent)) {
                elementGraph.addEdge(element.parent, element, GrammarOrRefEdge(null))
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
                it.accept(object : PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        elementGraph.addVertex(element)
                        val reference = element.reference ?: return
                        val targets = reference.multiResolveToElement()
                        val referenceInfos = reference.referenceInfos
                        require(targets.size == referenceInfos.size)
                        for ((i, target) in targets.withIndex()) {
                            val referenceInfo = referenceInfos[i]
                            if (referenceInfo === UNKNOWN && element !== target
                                && reference !is KtDefaultAnnotationArgumentReference) {
                                reference.referenceInfos
                                logger.info { "unknown reference info at source: ${element.posStr()}, target: ${target.posStr()}" }
                            }
                            if (target in elementGraph.vertexSet() && referenceInfo != UNKNOWN) {
                                elementGraph.addEdge(element, target, GrammarOrRefEdge(referenceInfo))
                            }

                        }
                        super.visitElement(element)
                    }
                })
            }
        }
        logger.info { "analyze references cost: $analyzeReferencesTime" }
        logger.info { "start extract references" }
        val extractTime = measureTime {
            val collectEdgeFilter: (GrammarOrRefEdge) -> Boolean =
                { (it.referenceInfo != null) && (it.referenceInfo != UNKNOWN) }
            val extractor = TypedRefExtractor<PsiElement, GrammarOrRefEdge>(
                { it is PsiFile }, { it.referenceInfo == null },
                collectEdgeFilter,
                GrammarOrRefEdge::class.java
            )
            val graph = extractor.doExtractor(elementGraph, allPsiFiles)
            val set = hashSetOf<PsiElement>()
            for (element in graph.vertexSet()) {
                if (graph.edgesOf(element).none(collectEdgeFilter)) {
                    set.add(element)
                }
            }
            for (element in set) {
                graph.removeVertex(element)
            }
            val exporter = DOTExporter<PsiElement, GrammarOrRefEdge>()
            exporter.setVertexAttributeProvider {
                mapOf("label" to DefaultAttribute.createAttribute(it.toString()))
            }
            exporter.setEdgeAttributeProvider {
                mapOf("label" to DefaultAttribute.createAttribute(it.referenceInfo.toString()))
            }
            val writer: Writer = FileWriter(output)
            exporter.exportGraph(graph, writer)
            writer.flush()
        }
        logger.info { "extract references cost: $extractTime" }
    }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(RefExtract()).execute(*args))