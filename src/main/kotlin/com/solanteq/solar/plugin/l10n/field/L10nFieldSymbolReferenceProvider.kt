package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.solanteq.solar.plugin.l10n.FormL10nChain
import com.solanteq.solar.plugin.l10n.L10nReferenceContributor
import com.solanteq.solar.plugin.symbol.FormSymbolReference

class L10nFieldSymbolReferenceProvider : PsiSymbolReferenceProvider {

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): List<FormSymbolReference<*>> {
        if(!L10nReferenceContributor.l10nPropertyPattern.accepts(element)) return emptyList()
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10nChain.fromElement(stringLiteral) ?: return emptyList()

        val l10nFieldChain = l10nChain.fieldChain
        if(l10nFieldChain.isEmpty()) {
            return emptyList()
        }

        val group = l10nChain.referencedGroupElement ?: return emptyList()
        val fieldsInGroup = group.allFields
        if(fieldsInGroup.isEmpty()) {
            return emptyList()
        }

        val l10nFieldNameChain = l10nFieldChain.map { it.second }
        val offset = hints.offsetInElement

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