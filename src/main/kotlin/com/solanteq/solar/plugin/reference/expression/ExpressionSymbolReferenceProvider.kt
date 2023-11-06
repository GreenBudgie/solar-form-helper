package com.solanteq.solar.plugin.reference.expression

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.solanteq.solar.plugin.element.expression.ExpressionType
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import com.solanteq.solar.plugin.util.asList
import com.solanteq.solar.plugin.util.inForm
import com.solanteq.solar.plugin.util.isPropertyValueWithKey
import com.solanteq.solar.plugin.util.notJsonIncludeDeclaration

class ExpressionSymbolReferenceProvider : PsiSymbolReferenceProvider {

    private val pattern = inForm<JsonStringLiteral>()
        .notJsonIncludeDeclaration()
        .isPropertyValueWithKey(*ExpressionType.expressionProperties.toTypedArray())

    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): List<FormSymbolReference> {
        if(!pattern.accepts(element)) return emptyList()
        val stringLiteral = element as JsonStringLiteral
        return ExpressionSymbolReference(stringLiteral).asList()
    }

    override fun getSearchRequests(project: Project, target: Symbol) = emptyList<SearchRequest>()

}