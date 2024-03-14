package com.github.xyzboom.extractor

import com.intellij.psi.PsiElement
import org.jgrapht.nio.csv.CSVExporter
import org.jgrapht.nio.csv.VisioExporter
import org.jgrapht.nio.dimacs.DIMACSExporter
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.nio.gml.GmlExporter
import org.jgrapht.nio.graphml.GraphMLExporter
import org.jgrapht.nio.json.JSONExporter
import org.jgrapht.nio.lemon.LemonExporter
import org.jgrapht.nio.matrix.MatrixExporter
import picocli.CommandLine
import picocli.CommandLine.ITypeConverter
import java.util.*

private typealias Exporter = ExporterProxy<PsiElement, GrammarOrRefEdge>

class ExporterConverter : ITypeConverter<Exporter> {

    override fun convert(value: String): Exporter {
        val exporterKey = value.lowercase(Locale.getDefault())
        val result = supportedConverterMap[exporterKey]
            ?: throw CommandLine.TypeConversionException("must be one of ${supportedConverterMap.keys}, but $value")
        return result
    }

    companion object {
        private val supportedConverterMap = hashMapOf<String, Exporter>(
            "csv" to ExporterProxy("csv", CSVExporter()),
            "dimacs" to ExporterProxy("dimacs", DIMACSExporter()),
            "dot" to ExporterProxy("dot", DOTExporter()),
            "gml" to ExporterProxy("gml", GmlExporter()),
            "graphml" to ExporterProxy("graphml", GraphMLExporter()),
            "json" to ExporterProxy("json", JSONExporter()),
            "lemon" to ExporterProxy("lemon", LemonExporter()),
            "matrix" to ExporterProxy("matrix", MatrixExporter()),
            "visio" to ExporterProxy("visio", VisioExporter())
        )
    }

}