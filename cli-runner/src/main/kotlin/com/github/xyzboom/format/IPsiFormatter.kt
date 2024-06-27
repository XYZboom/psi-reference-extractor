package com.github.xyzboom.format

import com.intellij.psi.PsiElement
import org.jgrapht.nio.Attribute

fun interface IPsiFormatter {
    fun format(element: PsiElement): MutableMap<String, Attribute>
}