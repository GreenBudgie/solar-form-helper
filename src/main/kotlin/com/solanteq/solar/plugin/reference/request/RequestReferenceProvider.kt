package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.util.asArray
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes

object RequestReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        val stringLiteral = element as JsonStringLiteral

        val formRequest = getRequestElement(stringLiteral) ?: return emptyArray()

        return if(formRequest.isDropdownRequest) {
            getDropdownReferences(stringLiteral, formRequest)
        } else if(formRequest.hasGroup) {
            getCallableServiceReferences(stringLiteral, formRequest)
        } else {
            arrayOf(
                *getCallableServiceReferences(stringLiteral, formRequest),
                *getDropdownReferences(stringLiteral, formRequest)
            )
        }
    }

    private fun getDropdownReferences(element: JsonStringLiteral,
                                      formRequest: FormRequest): Array<out PsiReference> {
        val fullRangeEmptyReference = DropdownReference(
            element,
            element.textRangeWithoutQuotes,
            null
        ).asArray()

        val requestData = formRequest.requestData ?: return fullRangeEmptyReference
        val clazz = requestData.clazz ?: return fullRangeEmptyReference

        return DropdownReference(element, clazz.range, formRequest).asArray()
    }

    private fun getCallableServiceReferences(element: JsonStringLiteral,
                                             formRequest: FormRequest): Array<out PsiReference> {
        val fullRangeEmptyReference = CallableServiceReference(
            element,
            element.textRangeWithoutQuotes,
            null
        ).asArray()

        val requestData = formRequest.requestData ?: return fullRangeEmptyReference
        val module = requestData.module ?: return fullRangeEmptyReference
        val clazz = requestData.clazz ?: return fullRangeEmptyReference

        val serviceNameReference = CallableServiceReference(
            element,
            TextRange(module.range.startOffset, clazz.range.endOffset),
            formRequest
        )

        val method = requestData.method ?: return serviceNameReference.asArray()

        val serviceMethodReference = CallableMethodReference(
            element,
            method.range,
            formRequest
        )

        return arrayOf(serviceNameReference, serviceMethodReference)
    }

    private fun getRequestElement(valueElement: JsonStringLiteral): FormRequest? {
        val parentProperty = valueElement.parent as? JsonProperty ?: return null
        val requestProperty = if (parentProperty.name == "name") {
            parentProperty.parent?.parent as? JsonProperty ?: return null
        } else parentProperty

        return requestProperty.toFormElement()
    }

}