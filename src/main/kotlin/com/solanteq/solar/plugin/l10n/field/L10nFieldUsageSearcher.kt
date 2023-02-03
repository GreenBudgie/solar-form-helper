package com.solanteq.solar.plugin.l10n.field

import com.intellij.psi.search.SearchScope
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearcher

class L10nFieldUsageSearcher : FormSymbolUsageSearcher(FormSymbolType.FIELD) {

    override fun getQuery(target: FormSymbol, effectiveScope: SearchScope) =
        L10nFieldUsageSearchQuery(target, effectiveScope)

    override fun prepareSearchScope(initialScope: SearchScope) =
        L10nSearch.getL10nFilesScope(initialScope)

}