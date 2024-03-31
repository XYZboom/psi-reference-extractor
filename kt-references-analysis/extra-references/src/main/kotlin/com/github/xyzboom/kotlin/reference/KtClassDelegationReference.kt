package com.github.xyzboom.kotlin.reference

import org.jetbrains.kotlin.idea.references.AbstractKtReference
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry

abstract class KtClassDelegationReference(delegatedSuperTypeEntry: KtDelegatedSuperTypeEntry) :
    AbstractKtReference<KtDelegatedSuperTypeEntry>(delegatedSuperTypeEntry)