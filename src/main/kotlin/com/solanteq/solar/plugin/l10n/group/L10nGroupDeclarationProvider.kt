package com.solanteq.solar.plugin.l10n.group

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.asList
import com.solanteq.solar.plugin.util.inForm
import com.solanteq.solar.plugin.util.isInArrayWithKey
import com.solanteq.solar.plugin.util.isPropertyValueWithKey

class L10nGroupDeclarationProvider : PsiSymbolDeclarationProvider {

    private val groupNamePattern =
        inForm<JsonStringLiteral>()
        .isInArrayWithKey(FormGroup.ARRAY_NAME)
        .isPropertyValueWithKey("name")

    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): Collection<PsiSymbolDeclaration> {
        if(!groupNamePattern.accepts(element)) return emptyList()
        return FormSymbolDeclaration(
            element as JsonStringLiteral,
            FormSymbol.withFullTextRange(element, FormSymbolType.GROUP)
        ).asList()
    }

}