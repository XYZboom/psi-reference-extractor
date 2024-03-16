package com.github.xyzboom.extractor.converter

import com.github.xyzboom.extractor.GrammarOrRefEdge
import com.github.xyzboom.extractor.ReferenceInfo
import com.github.xyzboom.extractor.TypedRefExtractor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty

class GranularityConverter : ConverterFromMap<TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(supportedConverterMap) {
    companion object {
        private val supportedConverterMap = hashMapOf<String, TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(
            "file" to TypedRefExtractor("file", { it is PsiFile }, { it.referenceInfo == null },
                { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) },
                GrammarOrRefEdge::class.java
            ),
            "class" to TypedRefExtractor("class", { it is PsiClass || it is KtClass }, { it.referenceInfo == null },
                { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) },
                GrammarOrRefEdge::class.java
            ),
            "member" to TypedRefExtractor("member", {
                it is PsiMember || it is KtFunction || (it is KtProperty && !it.isLocal)
            }, { it.referenceInfo == null },
                { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) },
                GrammarOrRefEdge::class.java
            ),
            "expression" to TypedRefExtractor("expression", { true }, { it.referenceInfo == null },
                { (it.referenceInfo != null) && (it.referenceInfo != ReferenceInfo.UNKNOWN) },
                GrammarOrRefEdge::class.java
            )
        )
    }
}