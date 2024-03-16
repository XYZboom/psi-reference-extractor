package com.github.xyzboom.extractor.converter

import picocli.CommandLine
import picocli.CommandLine.ITypeConverter
import java.util.*

open class ConverterFromMap<T>(private val map: Map<String, T>) : ITypeConverter<T> {
    override fun convert(value: String): T {
        val exporterKey = value.lowercase(Locale.getDefault())
        val result = map[exporterKey]
            ?: throw CommandLine.TypeConversionException("must be one of ${map.keys}, but $value")
        return result
    }
}