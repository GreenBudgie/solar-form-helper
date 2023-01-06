package com.solanteq.solar.plugin.symbol

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.solanteq.solar.plugin.util.asListOrEmpty
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes

/**
 * Represents a reference to a single [FormSymbol] from [JsonStringLiteral].
 */
abstract class FormSymbolSingleReference(
    sourceElement: JsonStringLiteral,
    sourceElementTextRange: TextRange,
    val targetSymbol: FormSymbol?
) : FormSymbolReference(sourceElement, sourceElementTextRange) {

    constructor(
        sourceElement: JsonStringLiteral,
        sourceElementTextRange: TextRange,
        symbolType: FormSymbolType,
        targetElement: JsonStringLiteral?,
        targetElementTextRange: TextRange? = targetElement?.textRangeWithoutQuotes,
    ) : this(
        sourceElement,
        sourceElementTextRange,
        symbolFromElementAndRange(symbolType, targetElement, targetElementTextRange)
    )

    override fun resolveReference() = targetSymbol.asListOrEmpty()

    private companion object {

        fun symbolFromElementAndRange(
            symbolType: FormSymbolType,
            targetElement: JsonStringLiteral?,
            targetElementTextRange: TextRange?
        ): FormSymbol? {
            targetElement ?: return null
            targetElementTextRange ?: return null
            return FormSymbol.withElementTextRange(targetElement, targetElementTextRange, symbolType)
        }

    }

}