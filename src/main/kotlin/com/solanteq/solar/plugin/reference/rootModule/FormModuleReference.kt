package com.solanteq.solar.plugin.reference.rootModule

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class FormModuleReference(
    element: JsonStringLiteral
) : PsiReferenceBase<JsonStringLiteral>(element) {

    override fun getVariants(): Array<Any> {
        val directory = getDirectory() ?: return emptyArray()
        if(!directory.isDirectory) return emptyArray()
        val lookup = LookupElementBuilder.createWithIcon(directory)
        return arrayOf(lookup)
    }

    override fun resolve(): PsiElement? = getDirectory()

    private fun getDirectory() = element.containingFile?.originalFile?.parent

}