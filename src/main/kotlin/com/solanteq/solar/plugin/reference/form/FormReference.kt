package com.solanteq.solar.plugin.reference.form

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.util.findFormByFullName
import com.solanteq.solar.plugin.util.findNotIncludedForms
import com.solanteq.solar.plugin.util.getFormSolarName
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class FormReference(
    element: JsonStringLiteral,
    private val formFullName: String
) : PsiReferenceBase<JsonStringLiteral>(element)  {

    override fun getVariants() =
        findNotIncludedForms(element.project).mapNotNull { it.getFormSolarName() }.toTypedArray()

    override fun resolve() =
        findFormByFullName(element.project, formFullName)?.toPsiFile(element.project)

}