package com.solanteq.solar.plugin.reference.topLevelFile

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase

class FormTopLevelFileReference(
    element: JsonStringLiteral
) : PsiReferenceBase<JsonStringLiteral>(element) {

    override fun bindToElement(element: PsiElement): PsiElement {
        return element
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        if(!newElementName.endsWith(".json")) return super.handleElementRename(newElementName)
        return super.handleElementRename(newElementName.dropLast(5))
    }

    override fun getVariants(): Array<Any> {
        val file = element.containingFile?.originalFile ?: return emptyArray()
        val virtualFileName = file.virtualFile?.nameWithoutExtension ?: return emptyArray()
        val lookup = LookupElementBuilder
            .create(virtualFileName)
            .withIcon(file.getIcon(0))
        return arrayOf(lookup)
    }

    override fun resolve(): PsiFile? = element.containingFile

}