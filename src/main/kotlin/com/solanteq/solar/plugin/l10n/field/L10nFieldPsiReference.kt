package com.solanteq.solar.plugin.l10n.field

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.element.FormField

class L10nFieldPsiReference(
    val sourceElement: JsonStringLiteral,
    private val fieldsInGroup: List<FormField>,
    private val l10nFieldNameChain: List<String>,
    private val l10nFieldNameChainIndex: Int,
    textRange: TextRange,
) : PsiReferenceBase<JsonStringLiteral>(sourceElement, textRange, true) {

    override fun getVariants(): Array<Any> {
        val fieldProperties = L10nFieldSearcher.findApplicablePropertiesByNameChainAtIndex(
            fieldsInGroup,
            l10nFieldNameChain,
            l10nFieldNameChainIndex,
            true
        )
        return fieldProperties.map {
            LookupElementBuilder.create(it.name)
        }.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val fieldProperty = L10nFieldSearcher.findApplicablePropertiesByNameChainAtIndex(
            fieldsInGroup,
            l10nFieldNameChain,
            l10nFieldNameChainIndex,
            false
        ).firstOrNull() ?: return null
        return fieldProperty.referencedField?.sourcePsi
    }

}