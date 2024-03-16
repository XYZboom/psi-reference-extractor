package com.github.xyzboom.kotlin.reference

import org.jetbrains.kotlin.idea.references.AbstractKtReference
import org.jetbrains.kotlin.psi.KtProperty

abstract class KtPropertyTypedReference(ktProperty: KtProperty) : AbstractKtReference<KtProperty>(ktProperty)