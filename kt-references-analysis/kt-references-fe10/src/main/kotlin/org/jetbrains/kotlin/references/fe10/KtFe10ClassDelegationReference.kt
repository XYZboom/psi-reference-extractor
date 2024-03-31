/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.references.fe10

import com.github.xyzboom.kotlin.reference.KtClassDelegationReference
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtImportAlias
import org.jetbrains.kotlin.references.fe10.base.KtFe10Reference
import org.jetbrains.kotlin.resolve.BindingContext

class KtFe10ClassDelegationReference(
    ktDelegatedSuperTypeEntry: KtDelegatedSuperTypeEntry
) : KtClassDelegationReference(ktDelegatedSuperTypeEntry), KtFe10Reference {

    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        val delegateExpression = expression.delegateExpression!!
        val type = context.getType(delegateExpression) ?: return emptyList()
        val declarationDescriptor = type.constructor.declarationDescriptor ?: return emptyList()
        return setOf(declarationDescriptor)
    }

    override fun isReferenceToImportAlias(alias: KtImportAlias): Boolean {
        return super<KtFe10Reference>.isReferenceToImportAlias(alias)
    }

    override val resolvesByNames: Collection<Name>
        get() = listOf()
}