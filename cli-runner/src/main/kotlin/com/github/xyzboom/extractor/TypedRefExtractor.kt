package com.github.xyzboom.extractor

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedWeightedMultigraph
import org.jgrapht.traverse.BreadthFirstIterator

class TypedRefExtractor<V, E>(
    private val vertexFilter: (V) -> Boolean,
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

    fun doExtractor(graph: DirectedWeightedMultigraph<V, E>, startVertices: Iterable<V>): DirectedWeightedMultigraph<V, E> {
        val result = DirectedWeightedMultigraph<V, E>(edgeClass)
        graph.vertexSet().filter(vertexFilter).forEach(result::addVertex)
        val iterator = BreadthFirstIteratorWithEdgeFilter(graph, startVertices)
        val collectMap = HashMap<V, V>()
        fun getUpperVertex(v: V): V? {
            var collectV = v
            while (collectV != null && collectV !in result.vertexSet()) {
                collectV = iterator.getParent(collectV)
            }
            return collectV
        }

        while (iterator.hasNext()) {
            val v = iterator.next()
            val collectV = getUpperVertex(v) ?: continue
            collectMap[v] = collectV
        }
        val iterator1 = BreadthFirstIteratorWithEdgeFilter(graph, startVertices)
        while (iterator1.hasNext()) {
            val v = iterator1.next()
            val collectV = collectMap[v] ?: continue
            val edges = graph.outgoingEdgesOf(v)
            for (edge in edges.filter(collectEdgeFilter)) {
                val target = graph.getEdgeTarget(edge)
                val collectTarget = collectMap[target] ?: continue
                if (collectTarget === collectV) continue
                result.addEdge(collectV, collectTarget, edge)
            }
        }
        return result
    }
}