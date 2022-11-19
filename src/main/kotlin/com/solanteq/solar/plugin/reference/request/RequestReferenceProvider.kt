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

object RequestReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val valueLiteral = element as JsonStringLiteral

        val requestElement = getRequestElement(valueLiteral) ?: return emptyArray()

        val requestData = requestElement.getRequestData()
            ?: return arrayOf(
                ServiceNameReference(
                    valueLiteral,
                    TextRange(1, element.textLength - 1),
                    null
                )
            )

        val delimiterPosition = requestData.groupName.length + requestData.serviceName.length + 3

        val serviceNameReference = ServiceNameReference(
            valueLiteral,
            TextRange(1, delimiterPosition - 1),
            requestElement
        )

        val serviceMethodReference = ServiceMethodReference(
            valueLiteral,
            TextRange(delimiterPosition, delimiterPosition + requestData.methodName.length),
            requestElement
        )

        println(requestElement.getRequestString())

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