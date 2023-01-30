package com.solanteq.solar.plugin.l10n.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.projectScope

class L10nFormPsiReference(
    private val l10nChain: FormL10n
) : PsiReferenceBase<JsonStringLiteral>(l10nChain.keyElement, l10nChain.formTextRange, false) {

    override fun bindToElement(element: PsiElement): PsiElement {
        return element
    }

    //TODO Extract duplicate code
    override fun handleElementRename(newElementName: String): PsiElement {
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        return super.handleElementRename(newElementName.dropLast(5))
    }

    override fun getVariants(): Array<Any> {
        l10nChain.moduleName ?: return emptyArray()
        return FormSearch
            .findRootFormsInModule(element.project.projectScope(), l10nChain.moduleName)
            .map {
                LookupElementBuilder
                    .create(it.nameWithoutExtension)
                    .withIcon(Icons.ROOT_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve() = l10nChain.referencedFormPsiFile

}