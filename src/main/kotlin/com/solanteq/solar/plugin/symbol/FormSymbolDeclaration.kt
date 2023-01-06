package com.solanteq.solar.plugin.symbol

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclaration

/**
 * Represents a declaration of [FormSymbol] as a property [JsonStringLiteral] value.
 */
open class FormSymbolDeclaration(
    val element: JsonStringLiteral,
    val symbolInElement: FormSymbol
) : PsiSymbolDeclaration {

    override fun getDeclaringElement(): JsonStringLiteral = element

    override fun getRangeInDeclaringElement() = symbolInElement.elementTextRange

    override fun getSymbol(): FormSymbol = symbolInElement

}