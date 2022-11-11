package com.solanteq.solar.plugin.reference.request

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch.SearchParameters
import com.intellij.util.Processor
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl

class ServiceNameQueryExecutor : QueryExecutorBase<PsiAnnotationMemberValue, SearchParameters>(true) {

    override fun processQuery(queryParameters: SearchParameters, consumer: Processor<in PsiAnnotationMemberValue>) {
        val elementToSearch = queryParameters.elementToSearch as? PsiAnnotationMemberValue ?: return
        consumer.process(elementToSearch)
    }

}