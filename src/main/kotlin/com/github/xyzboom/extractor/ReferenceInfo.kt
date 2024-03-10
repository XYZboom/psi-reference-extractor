package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.types.IReferenceSourceType
import com.github.xyzboom.extractor.types.IReferenceTargetType
import com.github.xyzboom.extractor.types.Unknown
import com.intellij.lang.Language
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
data class ReferenceInfo(
    val sourceLanguage: Language,
    val targetLanguage: Language?,
    /**
     * From the perspective of the source PsiElement, what is the type of the reference?
     */
    val sourceType: IReferenceSourceType,
    /**
     * From the perspective of the target PsiElement, what is the type of the reference?
     *
     * If the source language is the same as the target, [targetType] and [sourceType] must be same.
     * Otherwise, maybe not.
     */
    val targetType: IReferenceTargetType?
) {
    companion object {
        @JvmStatic
        val UNKNOWN = ReferenceInfo(Language.ANY, null, Unknown, null)
    }

    override fun toString(): String {
        return "ReferenceInfo(${sourceLanguage.displayName} $sourceType ${targetLanguage?.displayName} $targetType)"
    }
}