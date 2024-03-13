package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.ReferenceInfo.Companion.UNKNOWN
import com.github.xyzboom.extractor.types.*
import com.github.xyzboom.extractor.types.Annotation
import com.github.xyzboom.extractor.types.Call
import com.github.xyzboom.extractor.types.Class
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaReference
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceExpression
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

private const val SourceKeyName = "KeySourceReferenceInfos\$Extractor"
val sourceReferenceInfoUserDataKey = Key.create<List<ReferenceInfo>>(SourceKeyName)

fun PsiReference.multiResolveToElement(): List<PsiElement> {
    return if (this is PsiPolyVariantReference) {
        multiResolve(false).map { it.element }
    } else {
        listOf(resolve())
    }.filterNotNull()
}

val PsiReference?.referenceInfos: List<ReferenceInfo>
    get() {
        this ?: return listOf()
        val source = element
        val data = source.getUserData(sourceReferenceInfoUserDataKey)
        if (data != null) return data
        val resolvedTargets = multiResolveToElement()

        val referenceInfo: List<ReferenceInfo> = when (this) {
            is KtReference -> {
                if (resolvedTargets.isNotEmpty()) getReferenceInfos(resolvedTargets)
                else {
                    val references = source.references
                    for (ref in references) {
                        val resolved = ref.multiResolveToElement()
                        if (resolved.isNotEmpty() && ref is KtReference) {
                            return ref.getReferenceInfos(resolved)
                        }
                    }
                    listOf()
                }
            }

            is PsiJavaReference -> getReferenceInfos(resolvedTargets)
            else -> listOf()
        }
        if (referenceInfo.isNotEmpty() && referenceInfo.any { it !== UNKNOWN }) {
            source.putUserData(sourceReferenceInfoUserDataKey, referenceInfo)
        }
        return referenceInfo
    }

fun PsiJavaReference.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> =
    when {
        this is PsiReferenceExpression -> getReferenceInfos(resolvedTargets)
        element.parent is PsiNewExpression -> resolvedTargets.map { resolvedTarget ->
            ReferenceInfo(
                JavaLanguage.INSTANCE, Expression, Create,
                resolvedTarget.language, resolvedTarget.targetType
            )
        }

        else -> listOf()
    }

private fun PsiReferenceExpression.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> =
    if (parent is PsiMethodCallExpression) {
        if (resolvedTargets.isNotEmpty()) {
            resolvedTargets.map { resolvedTarget ->
                if (resolvedTarget.language === JavaLanguage.INSTANCE) {
                    ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, JavaLanguage.INSTANCE, Method)
                } else if (resolvedTarget.language === KotlinLanguage.INSTANCE) {
                    val targetType = if (resolvedTarget is KtProperty) Property else Method
                    ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, KotlinLanguage.INSTANCE, targetType)
                } else UNKNOWN
            }
        } else {
            listOf(ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, null, null))
        }
    } else listOf(UNKNOWN)

private fun KtReference.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> =
    when (this) {
        is KDocReference -> listOf(UNKNOWN)
        is SyntheticPropertyAccessorReference -> getSyntheticPropertyAccessorReferenceInfo(resolvedTargets)
        is KtArrayAccessReference -> getKtArrayAccessReferenceInfo(resolvedTargets)
        is KtCollectionLiteralReference -> listOf(UNKNOWN)
        is KtConstructorDelegationReference -> {
            resolvedTargets.map { resolvedTarget ->
                require(resolvedTarget.targetType == Constructor)
                ReferenceInfo(KotlinLanguage.INSTANCE, Class, Call, resolvedTarget.language, Constructor)
            }
        }

        is KtDestructuringDeclarationReference -> listOf(UNKNOWN)
        is KtForLoopInReference, is KtInvokeFunctionReference -> {
            resolvedTargets.map { resolvedTarget ->
                require(resolvedTarget.targetType == Method)
                ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Call, resolvedTarget.language, Method)
            }
        }

        is KtPropertyDelegationMethodsReference -> {
            resolvedTargets.map { resolvedTarget ->
                require(resolvedTarget.targetType == Method)
                ReferenceInfo(KotlinLanguage.INSTANCE, Property, PropertyDelegate, resolvedTarget.language, Method)
            }
        }

        is KtSimpleNameReference -> getReferenceInfos(resolvedTargets)
        is KtDefaultAnnotationArgumentReference -> listOf(UNKNOWN)
        else -> throw ExtractorException("Unsupported reference type: ${this::class.java}")
    }

private fun getKtArrayAccessReferenceInfo(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    return resolvedTargets.map { resolvedTarget ->
        val targetType = resolvedTarget.targetType
        when (targetType) {
            // kotlin array access operator reload
            Method -> ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Call, resolvedTarget.language, Method)
            else -> UNKNOWN
        }
    }
}

private fun getSyntheticPropertyAccessorReferenceInfo(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    return resolvedTargets.map { resolvedTarget ->
        val targetLanguage = resolvedTarget.language
        return@map if (targetLanguage === JavaLanguage.INSTANCE) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Access, JavaLanguage.INSTANCE, Method)
        } else if (targetLanguage === KotlinLanguage.INSTANCE) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Access, KotlinLanguage.INSTANCE, Property)
        } else UNKNOWN
    }
}

private fun KtSimpleNameReference.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    return resolvedTargets.map { resolvedTarget ->
        val targetLanguage = resolvedTarget.language
        val targetType = resolvedTarget.targetType
        return@map if (element is KtOperationReferenceExpression) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Operator, Call, targetLanguage, targetType)
        } else if (element.parent is KtCallExpression) {
            if (resolvedTarget is KtConstructor<*> || targetType == Class || targetType == Constructor) {
                ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Create, targetLanguage, targetType)
            } else {
                ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Call, targetLanguage, targetType)
            }
        } else if (element.getParentOfType<KtImportList>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, File, Import, targetLanguage, targetType)
        } else if (element.getParentOfType<KtSuperTypeEntry>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Class, Implement, targetLanguage, targetType)
        } else if (element.getParentOfType<KtSuperTypeCallEntry>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Class, Extend, targetLanguage, targetType)
        } else if (element.getParentOfType<KtAnnotationEntry>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Class, Create, targetLanguage, targetType)
        } else {
            val property = element.getParentOfType<KtProperty>(false)
            if (property != null) {
                if (property.isLocal) {
                    ReferenceInfo(KotlinLanguage.INSTANCE, Expression, LocalVariableTyped, targetLanguage, targetType)
                } else {
                    ReferenceInfo(KotlinLanguage.INSTANCE, Property, PropertyTyped, targetLanguage, targetType)
                }
            } else {
                ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Access, targetLanguage, targetType)
            }
        }
    }
}

val PsiElement.targetType: IReferenceTargetType?
    get() = when (this) {
        is KtConstructor<*> -> Constructor
        is KtClassOrObject ->
            when (this) {
                is KtClass -> when {
                    isInterface() -> Interface
                    isAnnotation() -> Annotation
                    else -> Class
                }

                else -> Class
            }

        is PsiClass ->
            when {
                isInterface -> Interface
                isAnnotationType -> Annotation
                else -> Class
            }

        is KtProperty -> Property
        is KtFunction -> Method
        is PsiMethod ->
            if (isConstructor) {
                Constructor
            } else Method

        is PsiFile -> File
        is PsiField -> Field
        else -> null
    }