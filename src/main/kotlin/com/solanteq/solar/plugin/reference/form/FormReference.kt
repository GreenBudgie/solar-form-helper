package com.solanteq.solar.plugin.reference.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.getFormModuleName
import com.solanteq.solar.plugin.util.getFormSolarName
import org.jetbrains.kotlin.idea.base.util.allScope

class FormReference(
    element: JsonStringLiteral,
    private val referencedForm: PsiFile?
) : PsiReferenceBase<JsonStringLiteral>(element)  {

    override fun handleElementRename(newElementName: String): PsiElement {
        val referencedFormModule = referencedForm?.getFormModuleName()
            ?: return super.handleElementRename(newElementName)
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        val nameWithoutExtension = newElementName.dropLast(5)
        return super.handleElementRename("$referencedFormModule.$nameWithoutExtension")
    }

    override fun getVariants(): Array<Any> {
        val currentFile = element.containingFile.originalFile.virtualFile
        return FormSearch.findRootForms(element.project.allScope())
            .filter {
                it != currentFile
            }.map {
                LookupElementBuilder
                    .create(it.getFormSolarName())
                    .withIcon(Icons.ROOT_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve() = referencedForm

}