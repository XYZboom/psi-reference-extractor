package com.github.xyzboom.extractor

import com.github.xyzboom.extractor.types.IReferenceType
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.references.fe10.*
import org.jetbrains.kotlin.references.fe10.base.KtFe10Reference

@Suppress("Unused")
val KtReference.referenceType: IReferenceType
    get() {
        if (this !is KtFe10Reference) {
            throw ExtractorException(
                "Unsupported Kotlin reference type: ${this::class.java}," +
                        "  ${KtFe10Reference::class.simpleName}"
            )
        }

        when (this) {
            is Fe10KDocReference -> {}
            is Fe10SyntheticPropertyAccessorReference -> {}
            is KtFe10ArrayAccessReference -> {}
            is KtFe10CollectionLiteralReference -> {}
            is KtFe10ConstructorDelegationReference -> {}
            is KtFe10DestructuringDeclarationEntry -> {}
            is KtFe10ForLoopInReference -> {}
            is KtFe10InvokeFunctionReference -> {}
            is KtFe10PropertyDelegationMethodsReference -> {}
            is KtFe10SimpleNameReference -> {}
            else -> throw ExtractorException("something wrong here!")
        }
        TODO()
    }

