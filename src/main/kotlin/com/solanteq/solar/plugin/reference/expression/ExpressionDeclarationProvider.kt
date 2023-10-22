package com.solanteq.solar.plugin.reference.expression

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.FormExpression
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.*

class ExpressionDeclarationProvider : PsiSymbolDeclarationProvider {

    private val expressionNamePattern =
        inForm<JsonStringLiteral>()
            .notJsonIncludeDeclaration()
            .isPropertyValueWithKey("name")
            .isInObjectInArrayWithKey(FormExpression.getArrayName())

    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): Collection<PsiSymbolDeclaration> {
        if(!expressionNamePattern.accepts(element)) return emptyList()
        return FormSymbolDeclaration(
            element as JsonStringLiteral,
            FormSymbol.withFullTextRange(element, FormSymbolType.EXPRESSION)
        ).asList()
    }

}