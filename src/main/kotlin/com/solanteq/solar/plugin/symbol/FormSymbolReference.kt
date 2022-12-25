package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.SingleTargetReference
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage

/**
 * Represents a reference to [FormSymbol] from any [PsiElement].
 */
open class FormSymbolReference<T : PsiElement>(
    private val sourceElement: T,
    private val textRange: TextRange,
    private val targetElement: JsonStringLiteral?
) : SingleTargetReference(), PsiSymbolReference {

    override fun resolveSingleTarget(): FormSymbol? = targetElement?.let { FormSymbol(it) }

    override fun getElement() = sourceElement

    override fun getRangeInElement() = textRange

    fun toPsiUsage() = PsiUsage.textUsage(this)

    fun toRenameUsage() = PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(
        toPsiUsage()
    )

}