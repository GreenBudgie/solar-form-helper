package com.solanteq.solar.plugin.l10n.field

import com.intellij.psi.search.SearchScope
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearcher
import com.solanteq.solar.plugin.util.restrictedByFormAndL10nFiles

class L10nFieldUsageSearcher : FormSymbolUsageSearcher(FormSymbolType.FIELD) {

    override fun getQuery(target: FormSymbol, effectiveScope: SearchScope) =
        L10nFieldUsageSearchQuery(target, effectiveScope)

    override fun prepareSearchScope(initialScope: SearchScope) =
        initialScope.restrictedByFormAndL10nFiles()

}