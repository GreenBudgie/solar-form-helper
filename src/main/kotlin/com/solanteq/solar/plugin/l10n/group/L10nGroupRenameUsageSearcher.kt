package com.solanteq.solar.plugin.l10n.group

import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.util.ListWrapperQuery
import com.solanteq.solar.plugin.util.asList

class L10nGroupRenameUsageSearcher : RenameUsageSearcher {

    override fun collectSearchRequests(parameters: RenameUsageSearchParameters): Collection<Query<out RenameUsage>> {
        val target = parameters.target
        if(target !is FormSymbol) return emptyList()
        val usages = L10nGroupReferencesSearch
            .findReferencesInAllScope(target)
            .map { it.toRenameUsage() }
        val declaration = target.toDeclarationRenameUsage()
        return ListWrapperQuery(usages + declaration).asList()
    }

}