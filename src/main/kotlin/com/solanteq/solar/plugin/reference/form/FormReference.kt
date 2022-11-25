package com.solanteq.solar.plugin.reference.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Assets
import com.solanteq.solar.plugin.util.findNotIncludedForms
import com.solanteq.solar.plugin.util.getFormSolarName

class FormReference(
    element: JsonStringLiteral,
    private val referencedForm: PsiFile?
) : PsiReferenceBase<JsonStringLiteral>(element)  {

    override fun getVariants() =
        findNotIncludedForms(element.project).map {
            LookupElementBuilder
                .create(it.getFormSolarName())
                .withIcon(Assets.FORM_ICON)
        }.toTypedArray()

    override fun resolve() = referencedForm

}