package com.github.xyzboom.extractor

import org.jgrapht.nio.Attribute
import org.jgrapht.nio.BaseExporter
import org.jgrapht.nio.GraphExporter
import org.jgrapht.nio.IntegerIdProvider
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class ExporterProxy<V, E>(
    val name: String,
    private val exporter: GraphExporter<V, E>,
) : BaseExporter<V, E>(IntegerIdProvider()), GraphExporter<V, E> by exporter {
    private val baseExporter: BaseExporter<V, E>

    init {
        require(exporter is BaseExporter<*, *>)
        @Suppress("UNCHECKED_CAST")
        baseExporter = exporter as BaseExporter<V, E>
    }

    override fun getGraphIdProvider(): Optional<Supplier<String>> {
        return baseExporter.graphIdProvider
    }

    override fun setGraphIdProvider(graphIdProvider: Supplier<String>?) {
        baseExporter.setGraphIdProvider(graphIdProvider)
    }

    override fun getGraphAttributeProvider(): Optional<Supplier<MutableMap<String, Attribute>>> {
        return baseExporter.graphAttributeProvider
    }

    override fun setGraphAttributeProvider(graphAttributeProvider: Supplier<MutableMap<String, Attribute>>?) {
        baseExporter.setGraphAttributeProvider(graphAttributeProvider)
    }

    override fun getVertexIdProvider(): Function<V, String> {
        return baseExporter.vertexIdProvider
    }

    override fun setVertexIdProvider(vertexIdProvider: Function<V, String>?) {
        baseExporter.setVertexIdProvider(vertexIdProvider)
    }

    override fun getVertexAttributeProvider(): Optional<Function<V, MutableMap<String, Attribute>>> {
        return baseExporter.vertexAttributeProvider
    }

    override fun setVertexAttributeProvider(vertexAttributeProvider: Function<V, MutableMap<String, Attribute>>?) {
        baseExporter.setVertexAttributeProvider(vertexAttributeProvider)
    }

    override fun getEdgeIdProvider(): Optional<Function<E, String>> {
        return baseExporter.edgeIdProvider
    }

    override fun setEdgeIdProvider(edgeIdProvider: Function<E, String>?) {
        baseExporter.setEdgeIdProvider(edgeIdProvider)
    }

    override fun getEdgeAttributeProvider(): Optional<Function<E, MutableMap<String, Attribute>>> {
        return baseExporter.edgeAttributeProvider
    }

    override fun setEdgeAttributeProvider(edgeAttributeProvider: Function<E, MutableMap<String, Attribute>>?) {
        baseExporter.setEdgeAttributeProvider(edgeAttributeProvider)
    }

    override fun getGraphId(): Optional<String> {
        return baseExporter.graphIdProvider.map { it.get() }
    }

    override fun getVertexId(v: V): String {
        return baseExporter.vertexIdProvider.apply(v)
    }

    override fun getEdgeId(e: E): Optional<String> {
        return baseExporter.edgeIdProvider.map { it.apply(e) }
    }

    override fun getVertexAttributes(v: V): Optional<MutableMap<String, Attribute>> {
        return baseExporter.vertexAttributeProvider.map { it.apply(v) }
    }

    override fun getVertexAttribute(v: V, key: String?): Optional<Attribute> {
        return baseExporter.vertexAttributeProvider.map { it.apply(v)[key] }
    }

    override fun getEdgeAttributes(e: E): Optional<MutableMap<String, Attribute>> {
        return baseExporter.edgeAttributeProvider.map { it.apply(e) }
    }

    override fun getEdgeAttribute(e: E, key: String?): Optional<Attribute> {
        return baseExporter.edgeAttributeProvider.map { it.apply(e)[key] }
    }

    override fun getGraphAttribute(key: String?): Optional<Attribute> {
        return baseExporter.graphAttributeProvider.map { it.get()[key] }
    }
}