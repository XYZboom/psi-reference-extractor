package com.github.xyzboom.extractor

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember
import org.jetbrains.kotlin.psi.*

internal fun isFile(ele: PsiElement): Boolean {
    return ele is PsiFile
}

internal fun isClass(it: PsiElement): Boolean {
    return it is PsiClass || it is KtClassOrObject
}

internal fun isPropertyInConstructor(it: PsiElement): Boolean {
    if (it !is KtParameter) {
        return false
    }
    return it.parent is KtParameterList && it.parent.parent is KtConstructor<*>
}

internal fun isNormalMember(it: PsiElement): Boolean {
    return it is PsiMember || it is KtNamedFunction || (it is KtProperty && !it.isLocal)
            || it is KtConstructor<*>
}