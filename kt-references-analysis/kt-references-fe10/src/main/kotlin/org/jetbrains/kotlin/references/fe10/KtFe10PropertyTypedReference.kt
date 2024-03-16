package org.jetbrains.kotlin.references.fe10

import com.github.xyzboom.kotlin.reference.KtPropertyTypedReference
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtImportAlias
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.references.fe10.base.KtFe10Reference
import org.jetbrains.kotlin.references.fe10.util.resolveToDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

class KtFe10PropertyTypedReference(ktProperty: KtProperty) : KtPropertyTypedReference(ktProperty), KtFe10Reference {
    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        val descriptor = expression.resolveToDescriptor(context) as? VariableDescriptor
        if (descriptor != null) {
            val declarationDescriptor = descriptor.type.constructor.declarationDescriptor
            return if (declarationDescriptor != null) {
                listOf(declarationDescriptor)
            } else emptyList()
        }
        return emptyList()
    }

    override fun isReferenceToImportAlias(alias: KtImportAlias): Boolean {
        return super<KtFe10Reference>.isReferenceToImportAlias(alias)
    }

    override val resolvesByNames: Collection<Name>
        get() = listOf()

}