package com.github.xyzboom.extractor.converter

import com.github.xyzboom.extractor.GrammarOrRefEdge
import com.github.xyzboom.extractor.ReferenceInfo
import com.github.xyzboom.extractor.TypedRefExtractor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember
import org.jetbrains.kotlin.psi.*

class GranularityConverter : ConverterFromMap<TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(supportedConverterMap) {
    companion object {
        private fun isFile(ele: PsiElement): Boolean {
            return ele is PsiFile
        }

        private fun isClass(it: PsiElement): Boolean {
            return it is PsiClass || it is KtClassOrObject
        }

        private fun isMember(it: PsiElement): Boolean {
            return it is PsiMember || it is KtNamedFunction || (it is KtProperty && !it.isLocal)
        }

        private val supportedConverterMap = hashMapOf<String, TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(
            "file" to TypedRefExtractor(
                "file", ::isFile, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "class" to TypedRefExtractor(
                "class", ::isClass, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "member" to TypedRefExtractor(
                "member", ::isMember, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "structure" to TypedRefExtractor("structure", {
                isMember(it) || isClass(it) || isFile(it)
            }, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "expression" to TypedRefExtractor("expression", { true }, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            )
        )
    }
}