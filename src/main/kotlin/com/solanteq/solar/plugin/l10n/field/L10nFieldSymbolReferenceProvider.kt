package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.L10nReferenceContributor
import com.solanteq.solar.plugin.symbol.FormSymbolReference

class L10nFieldSymbolReferenceProvider : PsiSymbolReferenceProvider {

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): List<FormSymbolReference> {
        if(!L10nReferenceContributor.l10nPropertyPattern.accepts(element)) return emptyList()
        val stringLiteral = element as JsonStringLiteral
        val offset = hints.offsetInElement

        val existingPsiReference = element.findReferenceAt(offset)
        if(existingPsiReference is PsiMultiReference) {
            val fieldReference = existingPsiReference.references.find { it is L10nFieldPsiReference }
            val referencedField = fieldReference?.resolve()
            if(referencedField != null) {
                //We don't need a symbol reference if the real field exists
                return emptyList()
            }
        }

        val l10nChain = FormL10n.fromElement(stringLiteral) ?: return emptyList()

        val l10nFieldChain = l10nChain.fieldChain
        if(l10nFieldChain.isEmpty()) {
            return emptyList()
        }

        val group = l10nChain.referencedGroupElement ?: return emptyList()
        val fieldsInGroup = group.fields
        if(fieldsInGroup.isEmpty()) {
            return emptyList()
        }

        val l10nFieldNameChain = l10nFieldChain.strings

        return l10nFieldChain.mapIndexedNotNull { index, (textRange, _) ->
            if(offset != -1 && !textRange.contains(offset)) {
                return@mapIndexedNotNull null
            }

            L10nFieldSymbolReference(
                element,
                textRange,
                fieldsInGroup,
                l10nFieldNameChain,
                index
            )
        }
    }

    override fun getSearchRequests(project: Project, target: Symbol) = emptyList<SearchRequest>()

}