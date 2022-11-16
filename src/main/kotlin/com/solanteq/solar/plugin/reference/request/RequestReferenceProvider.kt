package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormRequest

object RequestReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonElement = element as JsonStringLiteral

        val requestData = FormRequest.parseRequestString(jsonElement.value)
            ?: return arrayOf(
                ServiceNameReference(
                    jsonElement,
                    TextRange(1, element.textLength - 1),
                    null
                )
            )

        val delimiterPosition = requestData.groupName.length + requestData.serviceName.length + 3

        val serviceNameReference = ServiceNameReference(
            jsonElement,
            TextRange(1, delimiterPosition - 1),
            requestData
        )

        val serviceMethodReference = ServiceMethodReference(
            jsonElement,
            TextRange(delimiterPosition, delimiterPosition + requestData.methodName.length),
            requestData
        )

        return arrayOf(serviceNameReference, serviceMethodReference)
    }

}