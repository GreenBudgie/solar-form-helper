package com.solanteq.solar.plugin.reference.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import org.jetbrains.uast.UField

class FieldReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val field: PsiElement
) : PsiReferenceBase<JsonStringLiteral>(element, textRange) {

    override fun resolve() = field

}