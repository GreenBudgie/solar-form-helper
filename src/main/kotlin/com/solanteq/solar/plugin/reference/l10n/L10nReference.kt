package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class L10nReference(
    element: JsonStringLiteral,
    textRange: TextRange,
) : PsiReferenceBase<JsonStringLiteral>(element, textRange, false) {

    override fun resolve(): PsiElement? {
        TODO("Not yet implemented")
    }

}