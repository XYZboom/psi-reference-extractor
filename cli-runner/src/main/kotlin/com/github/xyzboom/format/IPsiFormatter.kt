package com.github.xyzboom.format

import com.intellij.psi.PsiElement

fun interface IPsiFormatter {
    fun format(element: PsiElement): String?
}