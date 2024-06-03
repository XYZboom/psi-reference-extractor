package com.github.xyzboom.extractor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.graph.DirectedWeightedMultigraph
import org.jgrapht.traverse.BreadthFirstIterator

private val logger = KotlinLogging.logger{}

class TypedRefExtractor<V, E>(
    val name: String,
    /**
     * filter source vertex with given filter.
     */
    private val sourceVertexFilter: (V, E) -> Boolean,
    private val targetVertexFilter: (V, E) -> Boolean,
    private val traverseEdgeFilter: (E) -> Boolean,
    private val collectEdgeFilter: (E) -> Boolean,
    private val edgeClass: Class<out E>,
    private val extractNested: Boolean = false
) {

    inner class BreadthFirstIteratorWithEdgeFilter(
        graph: Graph<V, E>,
        startVertices: Iterable<V>
    ) : BreadthFirstIterator<V, E>(graph, startVertices) {
        override fun encounterVertex(vertex: V, edge: E) {
            if (edge == null || traverseEdgeFilter(edge))
                super.encounterVertex(vertex, edge)
        }
    }

    fun doExtractor(
        graph: DirectedWeightedMultigraph<V, E>,
        startVertices: Iterable<V>
    ): DirectedWeightedMultigraph<V, E> {
        val result = DirectedWeightedMultigraph<V, E>(edgeClass)
        val iterator = BreadthFirstIteratorWithEdgeFilter(graph, startVertices)

        while (iterator.hasNext()) {
            iterator.next()
        }

        fun collect(element: V, edge: E, filter: (V, E) -> Boolean): V? {
            var collect: V? = element
            while (collect != null && !filter(collect, edge)) {
                collect = try {
                    iterator.getParent(collect)
                } catch (e: NullPointerException) {
                    null
                }
            }
            return collect
        }

        val iterator1 = BreadthFirstIteratorWithEdgeFilter(graph, startVertices)
        while (iterator1.hasNext()) {
            val source: V = iterator1.next()
            val edges = graph.outgoingEdgesOf(source)
            for (edge in edges.filter(collectEdgeFilter)) {
                val target = graph.getEdgeTarget(edge)
                val collectSource = collect(source, edge, sourceVertexFilter) ?: run {
                    logger.trace { "no source collect found! source: $source" }
                    null
                } ?: continue
                val collectTarget = collect(target, edge, targetVertexFilter) ?: run {
                    logger.trace { "no target collect found! source: $source, target: $target" }
                    null
                } ?: continue
                if (collectTarget === collectSource) continue
                if (collectSource !in result.vertexSet()) {
                    result.addVertex(collectSource)
                }
                if (collectTarget !in result.vertexSet()) {
                    result.addVertex(collectTarget)
                }
                result.addEdge(collectSource, collectTarget, edge)
            }
        }
        return result
    }
}