package com.solanteq.solar.plugin.reference.include

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.element.toFormElement

object JsonIncludeReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val jsonIncludeElement = stringLiteral.toFormElement<FormJsonInclude>() ?: return emptyArray()

        val virtualFileChain = jsonIncludeElement.virtualFileChain
        val references = virtualFileChain.mapIndexed { index, (range, referencedFile) ->
            JsonIncludeReference(
                element,
                range,
                referencedFile,
                index,
                jsonIncludeElement
            )
        }

        return references.toTypedArray()
    }

}