package com.solanteq.solar.plugin.l10n.module

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.util.asArray

object L10nModulePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val l10nChain = FormL10n.fromElement(stringLiteral) ?: return emptyArray()

        return L10nModulePsiReference(l10nChain).asArray()
    }

}