package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.ReferenceInfo.Companion.UNKNOWN
import com.github.xyzboom.extractor.types.Call
import com.github.xyzboom.extractor.types.Method
import com.github.xyzboom.extractor.types.Property
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaReference
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceExpression
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry
import org.jetbrains.kotlin.psi.KtProperty

private const val SourceKeyName = "KeySourceReferenceInfo\$Extractor"
private const val TargetKeyName = "KeyTargetReferenceInfo\$Extractor"
private val sourceReferenceInfoUserDataKey = Key.create<ReferenceInfo>(SourceKeyName)
private val targetReferenceInfoUserDataKey = Key.create<ReferenceInfo>(TargetKeyName)

@Suppress("Unused")
val PsiReference.referenceInfo: ReferenceInfo
    get() {
        val source = element
        val data = source.getUserData(sourceReferenceInfoUserDataKey)
        if (data != null) return data
        val resolvedTarget = resolve()
        val referenceInfo = when (this) {
            is KtReference -> getReferenceInfo(resolvedTarget)
            is PsiJavaReference -> getReferenceInfo(resolvedTarget)
            else -> UNKNOWN
        }
        if (referenceInfo !== UNKNOWN) {
            source.putUserData(sourceReferenceInfoUserDataKey, referenceInfo)
            resolvedTarget?.putUserData(targetReferenceInfoUserDataKey, referenceInfo)
            return referenceInfo
        }
        return UNKNOWN
    }

fun PsiJavaReference.getReferenceInfo(resolvedTarget: PsiElement?): ReferenceInfo = when (this) {
    is PsiReferenceExpression -> getReferenceInfo(resolvedTarget)

    else -> UNKNOWN
}

private fun PsiReferenceExpression.getReferenceInfo(resolvedTarget: PsiElement?): ReferenceInfo =
    if (parent is PsiMethodCallExpression) {
    if (resolvedTarget != null) {
        if (resolvedTarget.language === JavaLanguage.INSTANCE) {
            ReferenceInfo(JavaLanguage.INSTANCE, JavaLanguage.INSTANCE, Call, Method)
        } else if (resolvedTarget.language === KotlinLanguage.INSTANCE) {
            val targetType = if (resolvedTarget is KtProperty) Property else Method
            ReferenceInfo(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE, Call, targetType)
        } else UNKNOWN
    } else {
        ReferenceInfo(JavaLanguage.INSTANCE, null, Call, null)
    }
} else UNKNOWN

private fun KtReference.getReferenceInfo(resolvedTarget: PsiElement?): ReferenceInfo =
    when (this) {
        is KDocReference -> UNKNOWN
        is SyntheticPropertyAccessorReference -> getReferenceInfo(resolvedTarget)
        is KtArrayAccessReference -> UNKNOWN
        is KtCollectionLiteralReference -> UNKNOWN
        is KtConstructorDelegationReference -> UNKNOWN
        is KtDestructuringDeclarationEntry -> UNKNOWN
        is KtForLoopInReference -> UNKNOWN
        is KtInvokeFunctionReference -> UNKNOWN
        is KtPropertyDelegationMethodsReference -> UNKNOWN
        is KtSimpleNameReference -> UNKNOWN
        else -> throw ExtractorException("Unsupported reference type: ${this::class.java}")
    }

private fun SyntheticPropertyAccessorReference.getReferenceInfo(resolvedTarget: PsiElement?): ReferenceInfo {
    if (resolvedTarget != null) {
        val targetLanguage = resolvedTarget.language
        if (targetLanguage === JavaLanguage.INSTANCE) {
            return ReferenceInfo(KotlinLanguage.INSTANCE, JavaLanguage.INSTANCE, Property, Method)
        } else if (targetLanguage === KotlinLanguage.INSTANCE) {
            return ReferenceInfo(KotlinLanguage.INSTANCE, KotlinLanguage.INSTANCE, Property, Property)
        }
        return UNKNOWN
    } else {
        return UNKNOWN
    }
}