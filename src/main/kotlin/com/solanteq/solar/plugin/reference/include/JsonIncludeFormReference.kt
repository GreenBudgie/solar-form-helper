package com.solanteq.solar.plugin.reference.include

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Assets
import com.solanteq.solar.plugin.util.findIncludedForms
import com.solanteq.solar.plugin.util.getFormModule
import com.solanteq.solar.plugin.util.getFormSolarName

class JsonIncludeFormReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val referencedForm: PsiFile?
) : PsiReferenceBase<JsonStringLiteral>(element, textRange)  {

    override fun getVariants() =
        findIncludedForms(element.project)
            .map {
                LookupElementBuilder
                    .create(it.getFormSolarName())
                    .withTailText(" in ${it.getFormModule()}")
                    .withIcon(Assets.INCLUDED_FORM_ICON)
            }.toTypedArray()

    override fun resolve() = referencedForm

}