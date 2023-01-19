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
        .isInArrayWithKey(FormField.ARRAY_NAME)
        .isPropertyValueWithKey("name")
        .notJsonIncludeDeclaration()

    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): Collection<FormSymbolDeclaration> {
        if(!fieldNamePattern.accepts(element)) return emptyList()

        val considerOffset = offsetInElement != -1

        val dotSplit = (element as JsonStringLiteral).dotSplit()
        val declarations = dotSplit.mapNotNull { (textRange, _) ->
            if(considerOffset && !textRange.contains(offsetInElement)) {
                return@mapNotNull null
            }

            val existingPsiReference = element.findReferenceAt(textRange.startOffset)
            if(existingPsiReference is FieldReference) {
                val referencedField = existingPsiReference.resolve()
                if(referencedField != null) {
                    //We don't need a symbol declaration if the real field exists
                    return@mapNotNull null
                }
            }

            return@mapNotNull FormSymbolDeclaration(
                element,
                FormSymbol.withElementTextRange(element, textRange, FormSymbolType.FIELD)
            )
        }

        return declarations
    }

}