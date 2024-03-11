package com.github.xyzboom.extractor

class GrammarOrRefEdge @JvmOverloads constructor(
    val referenceInfo: ReferenceInfo? = ReferenceInfo.UNKNOWN
) {
    override fun toString(): String {
        return referenceInfo.toString()
    }
}