package com.solanteq.solar.plugin.l10n.field

import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.asList
import org.jetbrains.kotlin.idea.base.util.allScope

class L10nFieldRenameUsageSearcher : RenameUsageSearcher {

    override fun collectSearchRequests(
        parameters: RenameUsageSearchParameters
    ): Collection<Query<out RenameUsage>> {
        val target = parameters.target
        if(target !is FormSymbol || target.type != FormSymbolType.FIELD) return emptyList()
        return L10nFieldUsageSearchQuery(target, parameters.project.allScope()).asList()
    }

}