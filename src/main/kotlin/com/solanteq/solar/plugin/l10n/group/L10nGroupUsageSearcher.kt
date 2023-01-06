package com.solanteq.solar.plugin.l10n.group

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.util.Query
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
import com.solanteq.solar.plugin.util.ListWrapperQuery
import com.solanteq.solar.plugin.util.asList
import org.jetbrains.kotlin.psi.psiUtil.contains

class L10nGroupUsageSearcher : UsageSearcher {

    override fun collectSearchRequests(parameters: UsageSearchParameters): Collection<Query<out Usage>> {
        val target = parameters.target
        if(target !is FormSymbol || target.type != FormSymbolType.GROUP) return emptyList()
        val usages = L10nGroupReferencesSearch
            .findReferencesInAllScope(target)
            .map { FormSymbolUsage(it) }
        val declaration = FormSymbolUsage(target, true)
        val allUsages = usages + declaration
        val scope = parameters.searchScope
        val usagesInScope = allUsages.filter {
            scope.contains(it.file)
        }
        return ListWrapperQuery(usagesInScope).asList()
    }

}