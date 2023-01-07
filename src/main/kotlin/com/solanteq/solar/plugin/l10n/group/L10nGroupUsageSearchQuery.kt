package com.solanteq.solar.plugin.l10n.group

import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.solanteq.solar.plugin.l10n.L10nService
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearchQuery
import org.jetbrains.kotlin.psi.psiUtil.contains

class L10nGroupUsageSearchQuery(
    resolveTarget: FormSymbol,
    searchScope: SearchScope
) : FormSymbolUsageSearchQuery(resolveTarget, searchScope){

    override fun processDeclarations(consumer: Processor<in FormSymbolUsage>): Boolean {
        if(resolveTarget.file !in searchScope) return true
        return consumer.process(FormSymbolUsage(resolveTarget, true))
    }

    override fun processReferences(globalSearchScope: GlobalSearchScope,
                                   consumer: Processor<in FormSymbolUsage>): Boolean {
        val formL10ns = L10nService.findFormL10ns(resolveTarget.project, globalSearchScope)
        val keys = formL10ns.map { it.keyElement }
        val symbolReferenceService = PsiSymbolReferenceService.getService()
        keys.forEach {
            val references = symbolReferenceService.getReferences(it)
            val groupReferences = references.filterIsInstance<L10nGroupSymbolReference>()
            if(!processReferences(groupReferences, consumer)) {
                return false
            }
        }
        return true
    }

}