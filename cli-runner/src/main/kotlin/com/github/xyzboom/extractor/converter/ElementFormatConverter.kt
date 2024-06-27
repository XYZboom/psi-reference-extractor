package com.github.xyzboom.extractor.converter

import com.github.xyzboom.format.IPsiFormatter

class ElementFormatConverter(
    qualifiedNameFormatter: IPsiFormatter,
    leafTextOnlyFormatter: IPsiFormatter,
    leafTextAndQualifiedName: IPsiFormatter
) : ConverterFromMap<IPsiFormatter>(
    buildMap {
        put("qualified-name", qualifiedNameFormatter)
        put("leaf-text-only", leafTextOnlyFormatter)
        put("leaf-text-and-qualified-name", leafTextAndQualifiedName)
    }
)