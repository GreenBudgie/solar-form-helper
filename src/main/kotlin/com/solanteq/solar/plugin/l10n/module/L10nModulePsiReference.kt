package com.solanteq.solar.plugin.l10n.module

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.l10n.FormL10nChain

class L10nModulePsiReference(
    val l10nChain: FormL10nChain
) : PsiReferenceBase<JsonStringLiteral>(l10nChain.element, l10nChain.moduleTextRange, false) {

    override fun resolve() = l10nChain.referencedModulePsiDirectory

}