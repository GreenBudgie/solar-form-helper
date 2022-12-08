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

        val referenceList = mutableListOf<L10nReference>()

        if(l10nChain.formNameTextRange != null) {
            referenceList += L10nReference(stringLiteral, l10nChain.formNameTextRange!!, l10nChain.formNameReference)
        }

        if(l10nChain.groupNameTextRange != null) {
            referenceList += L10nReference(stringLiteral, l10nChain.groupNameTextRange!!, l10nChain.groupNameReference)
        }

        return referenceList.toTypedArray()
    }

}