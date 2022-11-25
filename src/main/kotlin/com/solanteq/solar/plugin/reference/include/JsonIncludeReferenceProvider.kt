package com.solanteq.solar.plugin.reference.include

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.element.toFormElement
import org.jetbrains.kotlin.idea.core.util.toPsiFile

object JsonIncludeReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val jsonIncludeElement = stringLiteral.toFormElement<FormJsonInclude>() ?: return emptyArray()

        val stringLength = stringLiteral.value.length
        val formNameLength = jsonIncludeElement.formNameWithExtension?.length ?: return emptyArray()
        return arrayOf(
            JsonIncludeFormReference(
                stringLiteral,
                TextRange.create(stringLength - formNameLength - 1, stringLength - 1),
                jsonIncludeElement.referencedFormFile?.toPsiFile(stringLiteral.project)
            )
        )
    }

}