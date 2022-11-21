package com.solanteq.solar.plugin.reference.field

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.toFormElement

object FieldReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as JsonStringLiteral
        val parentObject = stringLiteral.parent?.parent as? JsonObject ?: return emptyArray()
        val formField = parentObject.toFormElement<FormField>() ?: return emptyArray()

        val stringPropertyChain = formField.stringPropertyChain ?: return emptyArray()
        val propertyChainAsJavaPsi = formField.propertyChain.mapNotNull { it.javaPsi }
        val stringToFieldChain = propertyChainAsJavaPsi.zip(stringPropertyChain)

        var currentPosition = 1

        return stringToFieldChain.map { (field, fieldName) ->
            val fieldLength = fieldName.length
            val rangeStart = currentPosition
            val rangeEnd = rangeStart + fieldLength
            currentPosition += fieldLength + 1
            FieldReference(
                stringLiteral,
                TextRange(rangeStart, rangeEnd),
                field
            )
        }.toTypedArray()
    }

}