package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

object RequestReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonElement = element as JsonStringLiteral
        val requestData = parseRequestString(jsonElement.value) ?: return emptyArray()

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