package com.github.xyzboom.extractor.converter

import com.github.xyzboom.format.IPsiFormatter

class ElementFormatConverter(
    qualifiedNameFormatter: IPsiFormatter,
    leafTextOnlyFormatter: IPsiFormatter
) : ConverterFromMap<IPsiFormatter>(
    buildMap {
        put("qualified-name", qualifiedNameFormatter)
        put("leaf-text-only", leafTextOnlyFormatter)
    }
)