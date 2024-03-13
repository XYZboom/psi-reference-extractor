package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.types.IReferenceSourceType
import com.github.xyzboom.extractor.types.IReferenceType
import com.github.xyzboom.extractor.types.IReferenceTargetType
import com.github.xyzboom.extractor.types.Unknown
import com.intellij.lang.Language
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
data class ReferenceInfo(
    val sourceLanguage: Language,
    val sourceType: IReferenceSourceType,
    val referenceType: IReferenceType,
    val targetLanguage: Language?,
    val targetType: IReferenceTargetType?
) {
    companion object {
        @JvmStatic
        val UNKNOWN = ReferenceInfo(Language.ANY, Unknown, Unknown, null, null)
    }

    override fun toString(): String {
        return "ReferenceInfo(${sourceLanguage.displayName.ifEmpty { "ANY" }} $sourceType $referenceType ${targetLanguage?.displayName} $targetType)"
    }
}