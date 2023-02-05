package com.solanteq.solar.plugin.l10n.group

import com.intellij.psi.search.SearchScope
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearcher
import com.solanteq.solar.plugin.util.restrictedByL10nFiles

class L10nGroupUsageSearcher : FormSymbolUsageSearcher(FormSymbolType.GROUP) {

    override fun getQuery(target: FormSymbol, effectiveScope: SearchScope) =
        L10nGroupUsageSearchQuery(target, effectiveScope)

    override fun prepareSearchScope(initialScope: SearchScope) =
        initialScope.restrictedByL10nFiles()

}