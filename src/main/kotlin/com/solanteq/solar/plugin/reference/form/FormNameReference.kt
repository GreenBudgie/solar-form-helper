package com.solanteq.solar.plugin.reference.form

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class FormNameReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val moduleName: String,
    private val formName: String
) : PsiReferenceBase<JsonStringLiteral>(element, textRange)  {

    override fun handleElementRename(newElementName: String): PsiElement {
        if(!newElementName.endsWith(".json")) {
            return super.handleElementRename(newElementName)
        }
        val nameWithoutExtension = newElementName.dropLast(5)
        return super.handleElementRename(nameWithoutExtension)
    }

    override fun getVariants(): Array<Any> {
        val currentFile = element.containingFile.originalFile.virtualFile ?: return emptyArray()
        val project = element.project
        return FormSearch.findRootFormsInModule(project.allScope(), moduleName)
            .filter {
                it != currentFile
            }.map {
                LookupElementBuilder
                    .create(it.nameWithoutExtension)
                    .withIcon(Icons.ROOT_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve(): PsiFile? {
        val project = element.project
        return FormSearch.findFormByModuleAndName(
            moduleName,
            formName,
            project.allScope()
        )?.toPsiFile(project)
    }

}