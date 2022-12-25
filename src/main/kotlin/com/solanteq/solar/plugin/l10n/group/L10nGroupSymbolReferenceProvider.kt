package com.solanteq.solar.plugin.l10n.group

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.solanteq.solar.plugin.l10n.FormL10nChain
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import com.solanteq.solar.plugin.util.asListOrEmpty

class L10nGroupSymbolReferenceProvider : PsiSymbolReferenceProvider {

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): List<FormSymbolReference<*>> {
        if(!FormL10nChain.elementPattern.accepts(element)) return emptyList()
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10nChain.fromElement(stringLiteral) ?: return emptyList()

        if(l10nChain.groupNameTextRange == null) return emptyList()
        return getReferenceForOffset(stringLiteral, l10nChain, hints.offsetInElement).asListOrEmpty()
    }

    private fun getReferenceForOffset(
        element: JsonStringLiteral,
        l10nChain: FormL10nChain,
        offset: Int): FormSymbolReference<*>? {
        if(offset != -1 && l10nChain.groupNameTextRange?.contains(offset) == false) {
            return null
        }
        return FormSymbolReference(
            element,
            l10nChain.groupNameTextRange!!,
            l10nChain.groupNameReference
        )
    }

    override fun getSearchRequests(project: Project, target: Symbol) = emptyList<SearchRequest>()

}