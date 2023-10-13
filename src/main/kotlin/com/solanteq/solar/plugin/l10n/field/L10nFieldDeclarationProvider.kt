package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.reference.field.FieldReference
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.*

class L10nFieldDeclarationProvider : PsiSymbolDeclarationProvider {

    private val fieldNamePattern =
        inForm<JsonStringLiteral>()
            .notJsonIncludeDeclaration()
            .isPropertyValueWithKey("name")
            .isInObjectInArrayWithKey(FormField.getArrayName())

    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): Collection<FormSymbolDeclaration> {
        if(!fieldNamePattern.accepts(element)) return emptyList()

        val considerOffset = offsetInElement != -1

        val rangeSplit = RangeSplit.from(element as JsonStringLiteral)
        val declarations = rangeSplit.mapNotNull {
            if(considerOffset && !it.range.contains(offsetInElement)) {
                return@mapNotNull null
            }

            val existingPsiReference = element.findReferenceAt(it.range.startOffset)
            if(existingPsiReference is FieldReference) {
                val referencedField = existingPsiReference.resolve()
                if(referencedField != null) {
                    //We don't need a symbol declaration if the real field exists
                    return@mapNotNull null
                }
            }

            return@mapNotNull FormSymbolDeclaration(
                element,
                FormSymbol.withElementTextRange(element, it.range, FormSymbolType.FIELD)
            )
        }

        return declarations
    }

}