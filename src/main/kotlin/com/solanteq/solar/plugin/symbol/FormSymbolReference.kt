package com.solanteq.solar.plugin.symbol

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange

/**
 * Represents a reference to [FormSymbol] from [JsonStringLiteral].
 */
abstract class FormSymbolReference(
    private val sourceElement: JsonStringLiteral,
    private val sourceElementTextRange: TextRange
) : PsiSymbolReference {

    abstract override fun resolveReference(): List<FormSymbol>

    override fun getElement() = sourceElement

    override fun getRangeInElement() = sourceElementTextRange

}