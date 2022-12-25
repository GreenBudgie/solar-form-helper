package com.solanteq.solar.plugin.l10n.group

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.util.ListWrapperQuery

class L10nGroupRenameUsageSearcher : RenameUsageSearcher {

    override fun collectSearchRequests(parameters: RenameUsageSearchParameters): Collection<Query<out RenameUsage>> {
        val target = parameters.target
        val searchScope = parameters.searchScope as? GlobalSearchScope ?: return emptyList()
        if(target !is FormSymbol) return emptyList()
        val usages = L10nGroupReferencesSearch
            .findReferencesInAllScope(target)
            .map { it.toRenameUsage() }
        val declaration = target.toDeclarationRenameUsage()
        return listOf(ListWrapperQuery(usages + declaration))
    }

}