package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.ReferencesSearch.SearchParameters
import com.intellij.util.Processor
import com.solanteq.solar.plugin.util.serviceSolarName

class CallableServiceQueryExecutor : QueryExecutorBase<JsonStringLiteral, SearchParameters>(true) {

    override fun processQuery(queryParameters: SearchParameters, consumer: Processor<in JsonStringLiteral>) {
        val elementToSearch = queryParameters.elementToSearch as? PsiClass ?: return
        val solarName = elementToSearch.serviceSolarName ?: return
        queryParameters.optimizer.searchWord(
            solarName,
            queryParameters.effectiveSearchScope,
            true,
            elementToSearch
        )
    }

}