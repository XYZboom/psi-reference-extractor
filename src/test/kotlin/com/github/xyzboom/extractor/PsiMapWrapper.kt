package com.github.xyzboom.extractor

import com.intellij.psi.PsiElement
import org.junit.jupiter.api.fail

class PsiMapWrapper(private val map: MutableMap<String, PsiElement>) : MutableMap<String, PsiElement> by map {
    override fun get(key: String): PsiElement {
        return map[key] ?: fail("no element named: $key")
    }
}