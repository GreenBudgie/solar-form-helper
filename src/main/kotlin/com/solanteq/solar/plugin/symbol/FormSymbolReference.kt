package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage
import com.solanteq.solar.plugin.util.asListOrEmpty

/**
 * Represents a reference to [FormSymbol] from any [PsiElement].
 */
open class FormSymbolReference<T : PsiElement>(
    val sourceElement: T,
    val textRange: TextRange,
    val targetElement: JsonStringLiteral?
) : PsiSymbolReference {

    override fun resolveReference(): List<FormSymbol> =
        targetElement?.let { FormSymbol(it) }.asListOrEmpty()

    fun resolveSingleTarget(): FormSymbol? = resolveReference().firstOrNull()

    override fun getElement() = sourceElement

    override fun getRangeInElement() = textRange

    fun toPsiUsage() = PsiUsage.textUsage(this)

    fun toRenameUsage() = PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(
        toPsiUsage()
    )

}