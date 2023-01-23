package com.solanteq.solar.plugin.reference.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.search.FormModuleSearch
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory

class FormModuleReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val referencedForm: PsiFile?
) : PsiReferenceBase<JsonStringLiteral>(element, textRange)  {

    override fun getVariants(): Array<Any> {
        val project = element.project
        val modules = FormModuleSearch.findProjectRootFormModules(project) +
                FormModuleSearch.findLibrariesRootFormModules(project)
        return modules
            .mapNotNull { it.toPsiDirectory(project) }
            .map {
                LookupElementBuilder
                    .create(it.name)
                    .withIcon(it.getIcon(0))
            }.toTypedArray()
    }

    override fun resolve() = referencedForm?.parent

}