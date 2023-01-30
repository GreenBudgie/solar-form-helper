package com.solanteq.solar.plugin.l10n.module

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.util.isRootFormModule

class L10nModulePsiReference(
    private val l10nChain: FormL10n
) : PsiReferenceBase<JsonStringLiteral>(l10nChain.keyElement, l10nChain.moduleTextRange, false) {

    override fun bindToElement(element: PsiElement): PsiElement {
        if(element !is PsiDirectory || !element.isRootFormModule()) {
            throw IllegalArgumentException("Rebind can only be performed on root form module")
        }
        ElementManipulators.getManipulator(this.element).handleContentChange(
            this.element,
            this.rangeInElement,
            element.name
        )
        return element
    }

    override fun resolve() = l10nChain.referencedModulePsiDirectory

}