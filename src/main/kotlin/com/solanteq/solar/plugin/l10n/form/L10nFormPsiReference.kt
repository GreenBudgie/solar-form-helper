package com.solanteq.solar.plugin.l10n.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.l10n.FormL10nChain
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.search.projectScope

class L10nFormPsiReference(
    val l10nChain: FormL10nChain
) : PsiReferenceBase<JsonStringLiteral>(l10nChain.element, l10nChain.formTextRange, false) {

    //TODO Extract duplicate code
    override fun handleElementRename(newElementName: String): PsiElement {
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        return super.handleElementRename(newElementName.dropLast(5))
    }

    override fun getVariants(): Array<Any> {
        l10nChain.moduleName ?: return emptyArray()
        return FormSearch
            .findTopLevelFormsInModule(element.project.projectScope(), l10nChain.moduleName)
            .map {
                LookupElementBuilder
                    .create(it.nameWithoutExtension)
                    .withIcon(Icons.TOP_LEVEL_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve() = l10nChain.referencedFormPsiFile

}