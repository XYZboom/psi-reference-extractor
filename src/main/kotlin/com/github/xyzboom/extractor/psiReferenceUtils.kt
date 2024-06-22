package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.ReferenceInfo.Companion.UNKNOWN
import com.github.xyzboom.extractor.types.*
import com.github.xyzboom.extractor.types.Annotation
import com.github.xyzboom.extractor.types.Call
import com.github.xyzboom.extractor.types.Class
import com.github.xyzboom.extractor.utils.isExtension
import com.github.xyzboom.kotlin.reference.KtClassDelegationReference
import com.github.xyzboom.kotlin.reference.KtFunctionReturnReference
import com.github.xyzboom.kotlin.reference.KtPropertyTypedReference
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.elements.KtLightMember
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isPropertyParameter

private const val SourceKeyName = "KeySourceReferenceInfos\$Extractor"
val sourceReferenceInfoUserDataKey = Key.create<List<ReferenceInfo>>(SourceKeyName)

fun PsiReference.multiResolveToElement(): List<PsiElement> {
    return if (this is PsiPolyVariantReference) {
        multiResolve(false).map { it.element }
    } else {
        listOf(resolve())
    }.filter { it != null && it !== element }.filterNotNull()
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

        element.parent is PsiReferenceList ->
            when (element.parent.elementType) {
                JavaStubElementTypes.EXTENDS_LIST ->
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE, Class, Extend,
                            resolvedTarget.language, resolvedTarget.targetType
                        )
                    }

                JavaStubElementTypes.EXTENDS_BOUND_LIST -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE, Class, TypeParameter,
                            resolvedTarget.language, resolvedTarget.targetType
                        )
                    }
                }

                else -> resolvedTargets.map { UNKNOWN }
            }

        element.parent is PsiTypeElement -> {
            val parent2 = element.parent.parent
            val parent3 = parent2.parent
            val parent4 = parent3.parent
            when {
                parent2 is PsiLocalVariable -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            LocalVariable,
                            LocalVariableTyped,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiField -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            Field,
                            FieldTyped,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiMethod -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            Method,
                            Return,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiParameter && parent3 is PsiParameterList
                        && parent4 is PsiMethod -> {
                    val sourceType = if (parent4.isConstructor) {
                        Constructor
                    } else {
                        Method
                    }
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            sourceType,
                            Parameter,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiTypeElement || parent2 is PsiParameterList
                        || parent2 is PsiReferenceParameterList -> {
                    val sourceType = when {
                        element.getParentOfType<PsiLocalVariable>(false) != null -> {
                            LocalVariable
                        }

                        element.getParentOfType<PsiMethod>(false) != null -> {
                            Method
                        }

                        element.getParentOfType<PsiClass>(false) != null -> {
                            Class
                        }

                        else -> Unknown
                    }
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            sourceType,
                            TypeParameter,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiTypeCastExpression -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            Expression,
                            Cast,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                parent2 is PsiInstanceOfExpression -> {
                    resolvedTargets.map { resolvedTarget ->
                        ReferenceInfo(
                            JavaLanguage.INSTANCE,
                            Expression,
                            TypeCheck,
                            resolvedTarget.language,
                            resolvedTarget.targetType
                        )
                    }
                }

                else -> {
                    resolvedTargets.map {
                        UNKNOWN
                    }
                }
            }
        }

        element.parent is PsiImportStatement -> {
            resolvedTargets.map { resolvedTarget ->
                ReferenceInfo(
                    JavaLanguage.INSTANCE,
                    File,
                    Import,
                    resolvedTarget.language,
                    resolvedTarget.targetType
                )
            }
        }

        else -> resolvedTargets.map { resolvedTarget ->
            val sourceType = element.sourceType
            when (resolvedTarget.targetType) {
                Annotation -> ReferenceInfo(
                    JavaLanguage.INSTANCE,
                    sourceType,
                    Create,
                    resolvedTarget.language,
                    Annotation
                )

                else -> UNKNOWN
            }
        }
    }

private fun PsiReferenceExpression.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> =
    if (parent is PsiMethodCallExpression) {
        if (resolvedTargets.isNotEmpty()) {
            resolvedTargets.map { resolvedTarget ->
                if (resolvedTarget.language === JavaLanguage.INSTANCE) {
                    ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, JavaLanguage.INSTANCE, Method)
                } else if (resolvedTarget.language === KotlinLanguage.INSTANCE) {
                    val targetType = resolvedTarget.targetType
                    ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, KotlinLanguage.INSTANCE, targetType)
                } else UNKNOWN
            }
        } else {
            listOf(ReferenceInfo(JavaLanguage.INSTANCE, Expression, Call, null, null))
        }
    } else {
        if (children.size == 2 && children[1] is PsiIdentifier) {
            resolvedTargets.map {
                ReferenceInfo(JavaLanguage.INSTANCE, Expression, Access, it.language, it.targetType)
            }
        } else {
            // TODO some other cases
            resolvedTargets.map {
                ReferenceInfo(JavaLanguage.INSTANCE, Expression, Access, it.language, it.targetType)
            }
        }
    }

private fun KtReference.getReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> =
    when (this) {
        is KDocReference -> listOf(UNKNOWN)
        is SyntheticPropertyAccessorReference -> getSyntheticPropertyAccessorReferenceInfos(resolvedTargets)
        is KtArrayAccessReference -> getKtArrayAccessReferenceInfos(resolvedTargets)
        is KtCollectionLiteralReference -> listOf(UNKNOWN)
        is KtConstructorDelegationReference -> {
            resolvedTargets.map { resolvedTarget ->
                val targetType = resolvedTarget.targetType
                require(targetType == Constructor || targetType == Class)
                ReferenceInfo(KotlinLanguage.INSTANCE, Class, Call, resolvedTarget.language, targetType)
            }
        }

        is KtDestructuringDeclarationReference -> getDestructuringDeclarationReferenceInfos(resolvedTargets)
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
        is KtPropertyTypedReference -> getKtPropertyTypedReferenceInfos(resolvedTargets)
        is KtFunctionReturnReference -> getKtFunctionReturnReferenceInfos(resolvedTargets)
        is KtClassDelegationReference -> getKtClassDelegationReferenceInfos(resolvedTargets)
        else -> throw ExtractorException("Unsupported reference type: ${this::class.java}")
    }

private fun KtReference.getKtClassDelegationReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    val source = this.element
    val sourceLanguage = source.language
    return resolvedTargets.map { resolvedTarget ->
        val targetType = resolvedTarget.targetType
        ReferenceInfo(sourceLanguage, Class, ClassDelegate, resolvedTarget.language, targetType)
    }
}

private fun KtReference.getKtFunctionReturnReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    val source = this.element
    val sourceType = source.sourceType
    val sourceLanguage = source.language
    return resolvedTargets.map { resolvedTarget ->
        val targetType = resolvedTarget.targetType
        ReferenceInfo(sourceLanguage, sourceType, Return, resolvedTarget.language, targetType)
    }
}

private fun KtReference.getKtPropertyTypedReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    val source = this.element
    val sourceType = source.sourceType
    val sourceLanguage = source.language
    return resolvedTargets.map { resolvedTarget ->
        val targetType = resolvedTarget.targetType
        val referenceType = when {
            sourceType == Property && targetType == Class -> PropertyTyped
            sourceType == LocalVariable && targetType == Class -> LocalVariableTyped
            else -> Unknown
        }
        ReferenceInfo(sourceLanguage, sourceType, referenceType, resolvedTarget.language, targetType)
    }
}

private fun getDestructuringDeclarationReferenceInfos(resolvedTargets: List<PsiElement>) =
    resolvedTargets.map { resolvedTarget ->
        ReferenceInfo(
            KotlinLanguage.INSTANCE,
            Expression,
            DestructureCall,
            resolvedTarget.language,
            resolvedTarget.targetType
        )
    }

private fun getKtArrayAccessReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
    return resolvedTargets.map { resolvedTarget ->
        val targetType = resolvedTarget.targetType
        when (targetType) {
            // kotlin array access operator reload
            Method -> ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Call, resolvedTarget.language, Method)
            else -> UNKNOWN
        }
    }
}

private fun getSyntheticPropertyAccessorReferenceInfos(resolvedTargets: List<PsiElement>): List<ReferenceInfo> {
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
        } else if (element.isExtension) {
            ReferenceInfo(
                KotlinLanguage.INSTANCE,
                element.parent.parent.parent.sourceType,
                Extension,
                targetLanguage,
                targetType
            )
        } else if (element.getParentOfType<KtTypeParameter>(false) != null) {
            val sourceType = element.getParentOfType<KtTypeParameterList>(false)!!.parent.sourceType
            ReferenceInfo(
                KotlinLanguage.INSTANCE, sourceType,
                TypeParameter,
                targetLanguage, targetType
            )
        } else if (element.getParentOfType<KtImportList>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, File, Import, targetLanguage, targetType)
        } else if (element.getParentOfType<KtSuperTypeEntry>(false) != null) {
            when (targetType) {
                Class -> {
                    ReferenceInfo(KotlinLanguage.INSTANCE, Class, Extend, targetLanguage, targetType)
                }

                Interface -> {
                    ReferenceInfo(KotlinLanguage.INSTANCE, Class, Implement, targetLanguage, targetType)
                }

                else -> {
                    ReferenceInfo(KotlinLanguage.INSTANCE, Class, Unknown, targetLanguage, targetType)
                }
            }
        } else if (element.getParentOfType<KtSuperTypeCallEntry>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Class, Extend, targetLanguage, targetType)
        } else if (element.getParentOfType<KtAnnotationEntry>(false) != null) {
            ReferenceInfo(KotlinLanguage.INSTANCE, Class, Create, targetLanguage, targetType)
        } else if (element.getParentOfType<KtProperty>(false) != null) {
            if (element.getParentOfType<KtProperty>(false)!!.isLocal) {
                ReferenceInfo(KotlinLanguage.INSTANCE, LocalVariable, LocalVariableTyped, targetLanguage, targetType)
            } else {
                ReferenceInfo(KotlinLanguage.INSTANCE, Property, PropertyTyped, targetLanguage, targetType)
            }
        } else if (element.getParentOfType<KtParameterList>(false) != null) {
            val ktParameterList = element.getParentOfType<KtParameterList>(false)!!
            ReferenceInfo(
                KotlinLanguage.INSTANCE,
                ktParameterList.parent.sourceType,
                Parameter,
                targetLanguage,
                targetType
            )
        } else if (element.getParentOfType<KtBlockExpression>(false) == null
            && element.getParentOfType<KtFunction>(false) != null
        ) {
            ReferenceInfo(
                KotlinLanguage.INSTANCE,
                element.getParentOfType<KtFunction>(false)!!.sourceType,
                Return,
                targetLanguage,
                targetType
            )
        } else {
            ReferenceInfo(KotlinLanguage.INSTANCE, Expression, Access, targetLanguage, targetType)
        }
    }
}

val PsiElement.sourceType: IReferenceSourceType
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
                isAnnotationType -> Annotation
                isInterface -> Interface
                else -> Class
            }

        is KtProperty ->
            if (isLocal) {
                LocalVariable
            } else Property

        is KtFunction -> Method

        else -> when {
            parent is PsiAnnotation -> {
                val parent3 = parent.parent.parent
                when {
                    parent3 is PsiClass -> Class
                    parent3 is PsiField -> Field
                    parent3 is PsiMethod -> Method
                    else -> Unknown
                }
            }

            parent is PsiReferenceList && parent.elementType == JavaStubElementTypes.EXTENDS_LIST -> {
                Class
            }

            parent is PsiTypeElement -> {
                val parent2 = parent.parent
                when {
                    parent2 is PsiField -> Field
                    parent2 is PsiMethod -> Method
                    else -> Unknown
                }
            }

            else -> Unknown
        }
    }

val PsiElement.targetType: IReferenceTargetType
    get() = when (this) {
        is KtLightElement<*, *> ->
            if (kotlinOrigin != null) {
                @Suppress("RecursivePropertyAccessor")
                kotlinOrigin!!.targetType
            } else {
                Unknown
            }

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
                isAnnotationType -> Annotation
                isInterface -> Interface
                else -> Class
            }

        is KtProperty ->
            if (isLocal) {
                LocalVariable
            } else Property

        is KtFunction -> Method
        is PsiMethod ->
            if (isConstructor) {
                Constructor
            } else Method

        is PsiFile -> File
        is PsiField -> Field
        is KtParameter ->
            if (isPropertyParameter()) {
                Property
            } else Unknown

        else -> Unknown
    }