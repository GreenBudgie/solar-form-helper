package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.l10n.FormL10nChain

object L10nFieldPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10nChain.fromElement(stringLiteral) ?: return emptyArray()

        val l10nFieldChain = l10nChain.fieldChain
        if(l10nFieldChain.isEmpty()) {
            return emptyArray()
        }

        val group = l10nChain.referencedGroupElement ?: return emptyArray()
        val fieldsInGroup = group.allFields
        if(fieldsInGroup.isEmpty()) {
            return emptyArray()
        }

        val l10nFieldNameChain = l10nFieldChain.map { it.second }

        return l10nFieldChain.mapIndexed { index, (textRange, _) ->
            L10nFieldPsiReference(
                element,
                fieldsInGroup,
                l10nFieldNameChain,
                index,
                textRange
            )
        }.toTypedArray()
    }

}