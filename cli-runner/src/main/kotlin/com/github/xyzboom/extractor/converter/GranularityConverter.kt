package com.github.xyzboom.extractor.converter

import com.github.xyzboom.extractor.*
import com.github.xyzboom.extractor.types.Parameter
import com.intellij.psi.PsiElement

class GranularityConverter : ConverterFromMap<TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(supportedConverterMap) {
    companion object {
        private val myIsFile = wrapper<PsiElement, GrammarOrRefEdge, Boolean>(::isFile)
        private val myIsClass = wrapper<PsiElement, GrammarOrRefEdge, Boolean>(::isClass)
        private fun isSourceMember(element: PsiElement, edge: GrammarOrRefEdge): Boolean {
            val info = edge.referenceInfo ?: return true
            return when (info.referenceType) {
                Parameter -> isNormalMember(element)
                else -> isNormalMember(element) || isPropertyInConstructor(element)
            }
        }

        private fun isTargetMember(element: PsiElement, edge: GrammarOrRefEdge): Boolean {
            val info = edge.referenceInfo ?: return true
            return when (info.referenceType) {
                Parameter -> isClass(element)
                else -> isNormalMember(element) || isPropertyInConstructor(element)
            }
        }

        private val supportedConverterMap = hashMapOf<String, TypedRefExtractor<PsiElement, GrammarOrRefEdge>>(
            "file" to TypedRefExtractor(
                "file", myIsFile, myIsFile, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "class" to TypedRefExtractor(
                "class", myIsClass, myIsClass, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "member" to TypedRefExtractor(
                "member", ::isSourceMember, ::isTargetMember, { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "structure" to TypedRefExtractor("structure", { ele, edge ->
                isSourceMember(ele, edge) || isClass(ele) || isFile(ele)
            }, { ele, edge ->
                isTargetMember(ele, edge) || isClass(ele) || isFile(ele)
            },
                { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            ),
            "expression" to TypedRefExtractor(
                "expression", { _, _ -> true },
                { _, _ -> true },
                { it.referenceInfo == null },
                { it.referenceInfo != null },
                GrammarOrRefEdge::class.java
            )
        )
    }
}