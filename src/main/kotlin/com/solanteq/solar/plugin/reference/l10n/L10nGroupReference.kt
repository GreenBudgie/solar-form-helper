package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class L10nGroupReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val referencedElement: PsiElement?
) : L10nReference(element, textRange) {

    override fun resolve() = referencedElement

}