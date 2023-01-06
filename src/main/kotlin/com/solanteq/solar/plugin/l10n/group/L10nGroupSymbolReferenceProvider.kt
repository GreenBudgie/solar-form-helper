package com.solanteq.solar.plugin.l10n.group

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.L10nReferenceContributor
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import com.solanteq.solar.plugin.util.asListOrEmpty

class L10nGroupSymbolReferenceProvider : PsiSymbolReferenceProvider {

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): List<FormSymbolReference> {
        if(!L10nReferenceContributor.l10nPropertyPattern.accepts(element)) return emptyList()
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10n.fromElement(stringLiteral) ?: return emptyList()

        if(l10nChain.groupTextRange == null) return emptyList()
        return getReferenceForOffset(l10nChain, hints.offsetInElement).asListOrEmpty()
    }

    override fun getSearchRequests(project: Project, target: Symbol) = emptyList<SearchRequest>()

    private fun getReferenceForOffset(l10nChain: FormL10n, offset: Int): FormSymbolReference? {
        if(offset != -1 && !l10nChain.groupTextRange!!.contains(offset)) {
            return null
        }
        return L10nGroupSymbolReference(l10nChain)
    }
}