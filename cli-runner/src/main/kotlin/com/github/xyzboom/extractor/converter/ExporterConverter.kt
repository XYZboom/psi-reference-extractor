package com.github.xyzboom.extractor.converter

import com.github.xyzboom.extractor.ExporterProxy
import com.github.xyzboom.extractor.GrammarOrRefEdge
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

class ExporterConverter : ConverterFromMap<Exporter>(supportedConverterMap) {
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

private typealias Exporter = ExporterProxy<PsiElement, GrammarOrRefEdge>