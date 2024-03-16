package com.github.xyzboom.extractor.utils

import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

val KtSimpleNameExpression.isExtension : Boolean
    get() {
        val typeReferenceParent = parent.parent
        val callableDeclarationParent = typeReferenceParent.parent
        if (callableDeclarationParent !is KtCallableDeclaration) return false
        return callableDeclarationParent.receiverTypeReference == typeReferenceParent
    }