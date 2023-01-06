package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolReference

class L10nFieldSymbolReference(
    sourceElement: JsonStringLiteral,
    sourceElementTextRange: TextRange,
    private val fieldsInGroup: List<FormField>,
    private val l10nFieldNameChain: List<String>,
    private val l10nFieldNameChainIndex: Int,
) : FormSymbolReference(sourceElement, sourceElementTextRange) {

    override fun resolveReference(): List<FormSymbol> {
        val fieldProperties = L10nFieldSearcher.findApplicablePropertiesByNameChainAtIndex(
            fieldsInGroup,
            l10nFieldNameChain,
            l10nFieldNameChainIndex,
            false
        )
        return fieldProperties.mapNotNull { it.symbol }
    }

}