package com.solanteq.solar.plugin.l10n.field

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.l10n.FormL10n

object L10nFieldPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val formL10n = FormL10n.fromElement(stringLiteral) ?: return emptyArray()

        val l10nFieldChain = formL10n.fieldChain
        if(l10nFieldChain.isEmpty()) {
            return emptyArray()
        }

        val group = formL10n.referencedGroupElement ?: return emptyArray()
        val fieldsInGroup = group.fields
        if(fieldsInGroup.isEmpty()) {
            return emptyArray()
        }

        val l10nFieldNameChain = l10nFieldChain.strings

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