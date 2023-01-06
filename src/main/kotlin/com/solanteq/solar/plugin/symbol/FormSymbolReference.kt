package com.solanteq.solar.plugin.symbol

import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Represents a reference to [FormSymbol] from any [PsiElement].
 */
abstract class FormSymbolReference<T : PsiElement>(
    val sourceElement: T,
    val sourceElementTextRange: TextRange
) : PsiSymbolReference {

    abstract override fun resolveReference(): List<FormSymbol>

    override fun getElement() = sourceElement

    override fun getRangeInElement() = sourceElementTextRange

    fun getFirstResolveResult() = resolveReference().firstOrNull()

}