package com.solanteq.solar.plugin.reference.expression

import com.intellij.psi.search.SearchScope
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolRenameUsageSearcher
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.restrictedByFormFiles

class ExpressionRenameUsageSearcher : FormSymbolRenameUsageSearcher(FormSymbolType.EXPRESSION) {

    override fun getQuery(target: FormSymbol, effectiveScope: SearchScope) =
        ExpressionUsageSearchQuery(target, effectiveScope)

    override fun prepareSearchScope(initialScope: SearchScope) =
        initialScope.restrictedByFormFiles()

}