package com.solanteq.solar.plugin.reference.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Assets
import com.solanteq.solar.plugin.util.findTopLevelForms
import com.solanteq.solar.plugin.util.getFormModule
import com.solanteq.solar.plugin.util.getFormSolarName

class FormReference(
    element: JsonStringLiteral,
    private val referencedForm: PsiFile?
) : PsiReferenceBase<JsonStringLiteral>(element)  {

    override fun handleElementRename(newElementName: String): PsiElement {
        val referencedFormModule = referencedForm?.getFormModule()
            ?: return super.handleElementRename(newElementName)
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        val nameWithoutExtension = newElementName.dropLast(5)
        return super.handleElementRename(
            "$referencedFormModule.$nameWithoutExtension"
        )
    }

    override fun getVariants() =
        findTopLevelForms(element.project).map {
            LookupElementBuilder
                .create(it.getFormSolarName())
                .withIcon(Assets.TOP_LEVEL_FORM_ICON)
        }.toTypedArray()

    override fun resolve() = referencedForm

}