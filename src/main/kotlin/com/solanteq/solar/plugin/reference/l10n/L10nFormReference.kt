package com.solanteq.solar.plugin.reference.l10n

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.projectScope

class L10nFormReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val referencedForm: JsonFile?
) : L10nReference(element, textRange) {

    //TODO Extract duplicate code
    override fun handleElementRename(newElementName: String): PsiElement {
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        return super.handleElementRename(newElementName.dropLast(5))
    }

    override fun getVariants(): Array<Any> {
        return FormSearch.findTopLevelForms(element.project.projectScope())
            .map {
                LookupElementBuilder
                    .create(it.nameWithoutExtension)
                    .withIcon(Icons.TOP_LEVEL_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve() = referencedForm

}