package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage

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

    fun toPsiUsage() = PsiUsage.textUsage(this)

    fun toRenameUsage() = PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(
        toPsiUsage()
    )

}