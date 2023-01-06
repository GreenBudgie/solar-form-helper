package com.solanteq.solar.plugin.l10n.field

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.asList

class L10nFieldUsageSearcher : UsageSearcher {

    override fun collectSearchRequests(parameters: UsageSearchParameters): Collection<Query<out Usage>> {
        val target = parameters.target
        if(target !is FormSymbol || target.type != FormSymbolType.FIELD) return emptyList()
        return L10nFieldUsageSearchQuery(target, parameters.searchScope).asList()
    }

}