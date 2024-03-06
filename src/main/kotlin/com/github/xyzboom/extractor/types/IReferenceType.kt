package com.github.xyzboom.extractor.types

import com.intellij.lang.Language
import org.jetbrains.annotations.ApiStatus


interface IReferenceType {
    val sourceLanguage: Language
    val targetLanguage: Language

    /**
     * From the perspective of the source PsiElement, what is the type of the reference?
     */
    @get:ApiStatus.Experimental
    val sourceType: ReferenceType

    /**
     * From the perspective of the target PsiElement, what is the type of the reference?
     *
     * If the source language is the same as the target, [targetType] and [sourceType] must be same.
     * Otherwise, maybe not.
     */
    @get:ApiStatus.Experimental
    val targetType: ReferenceType
}