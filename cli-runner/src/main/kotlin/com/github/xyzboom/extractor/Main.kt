package com.github.xyzboom.extractor

import com.github.xyzboom.ktcutils.KotlinJvmCompilerContext
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import java.io.StringWriter
import java.io.Writer
import kotlin.system.exitProcess


@Command(name = "RefExtract")
class RefExtract : Runnable, KotlinJvmCompilerContext() {
    @Parameters(index = "0", description = ["input directory"])
    lateinit var input: File

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
        initCompilerEnv(input.toPath())
        visitAllPsiFiles {
            it.accept(AddAllElementVisitor())
        }
        visitAllPsiFiles {
            it.accept(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    elementGraph.addVertex(element)
                    val target = element.reference?.resolve()
                    if (target != null && target !is PsiPackage) {
                        elementGraph.addEdge(element, target, GrammarOrRefEdge(element.reference?.referenceInfo))
                    }
                    super.visitElement(element)
                }
            })
        }
        val collectEdgeFilter: (GrammarOrRefEdge) -> Boolean =
            { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) }
        val extractor = TypedRefExtractor<PsiElement, GrammarOrRefEdge>(
            { it is PsiFile }, { it.referenceInfo == null },
            collectEdgeFilter,
            GrammarOrRefEdge::class.java
        )
        val graph = extractor.doExtractor(elementGraph, allPsiFiles)
        println()
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
        val writer: Writer = StringWriter()
        exporter.exportGraph(graph, writer)
        println(writer.toString())
        println()
    }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(RefExtract()).execute(*args))