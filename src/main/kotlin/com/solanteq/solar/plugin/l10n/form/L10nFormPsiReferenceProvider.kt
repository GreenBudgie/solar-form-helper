package com.solanteq.solar.plugin.l10n.form

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.util.asArray

object L10nFormPsiReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10n.fromElement(stringLiteral) ?: return emptyArray()

        if(l10nChain.formTextRange == null) return emptyArray()
        return L10nFormPsiReference(l10nChain).asArray()
    }

}