package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

object L10nReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10nChain.fromElement(stringLiteral) ?: return emptyArray()

        val formNameReference = l10nChain.formNameReference
        val formTextRange = l10nChain.formTextRange

        if(formTextRange != null) {
            return arrayOf(L10nReference(stringLiteral, formTextRange, formNameReference))
        }
        return emptyArray()
    }

}