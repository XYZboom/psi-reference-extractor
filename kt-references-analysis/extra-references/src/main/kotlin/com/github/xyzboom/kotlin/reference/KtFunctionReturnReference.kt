package com.github.xyzboom.kotlin.reference

import org.jetbrains.kotlin.idea.references.AbstractKtReference
import org.jetbrains.kotlin.psi.KtFunction

abstract class KtFunctionReturnReference(ktFunction: KtFunction) : AbstractKtReference<KtFunction>(ktFunction)