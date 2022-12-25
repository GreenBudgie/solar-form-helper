package com.solanteq.solar.plugin.l10n.group

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.util.ListWrapperQuery

class L10nGroupUsageSearcher : UsageSearcher {

    override fun collectSearchRequests(parameters: UsageSearchParameters): Collection<Query<out Usage>> {
        val target = parameters.target
        val searchScope = parameters.searchScope as? GlobalSearchScope ?: return emptyList()
        if(target !is FormSymbol) return emptyList()
        val usages = L10nGroupReferencesSearch
            .findReferencesInAllScope(target)
            .map { it.toPsiUsage() }
        val declaration = target.toDeclarationUsage()
        return listOf(ListWrapperQuery(usages + declaration))
    }

}