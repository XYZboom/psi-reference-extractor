package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.ReferenceInfo.Companion.UNKNOWN
import com.github.xyzboom.extractor.types.Call
import com.github.xyzboom.extractor.types.Method
import com.github.xyzboom.extractor.types.Property
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiReference
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry

private const val KeyName = "KeyReferenceInfo\$Extractor"
private val referenceInfoUserDataKey = Key.create<ReferenceInfo>(KeyName)

@Suppress("Unused")
val PsiReference.referenceInfo: ReferenceInfo
    get() {
        val source = element
        val data = source.getUserData(referenceInfoUserDataKey)
        if (data != null) return data
        val referenceInfo = if (this is KtReference) {
             referenceInfo
        } else UNKNOWN
        if (referenceInfo !== UNKNOWN) {
            source.putUserData(referenceInfoUserDataKey, referenceInfo)
            return referenceInfo
        }
        return UNKNOWN
    }

val KtReference.referenceInfo: ReferenceInfo
    get() =
        when (this) {
            is KDocReference -> UNKNOWN
            is SyntheticPropertyAccessorReference -> referenceInfo
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

private inline val SyntheticPropertyAccessorReference.referenceInfo: ReferenceInfo
    get() {
        val target = resolve()
        if (target != null) {
            val targetLanguage = target.language
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