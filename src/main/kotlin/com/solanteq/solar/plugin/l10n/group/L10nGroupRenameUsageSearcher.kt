package com.solanteq.solar.plugin.l10n.group

import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.asList
import org.jetbrains.kotlin.idea.base.util.projectScope

class L10nGroupRenameUsageSearcher : RenameUsageSearcher {

    override fun collectSearchRequests(parameters: RenameUsageSearchParameters): Collection<Query<out RenameUsage>> {
        val target = parameters.target
        if(target !is FormSymbol || target.type != FormSymbolType.GROUP) return emptyList()
        return L10nGroupUsageSearchQuery(target, parameters.project.projectScope()).asList()
    }

}