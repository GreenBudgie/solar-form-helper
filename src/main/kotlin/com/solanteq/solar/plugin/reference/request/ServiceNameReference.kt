package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.util.findAllCallableServiceSolarNames
import org.jetbrains.uast.UClass

class ServiceNameReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestData: FormRequest.RequestData?
) : AbstractServiceReference(element, range, requestData) {

    override fun handleElementRename(newElementName: String): PsiElement {
        //Preventing rename of service solar name
        return element
    }

    override fun getVariants() = findAllCallableServiceSolarNames(element.project).toTypedArray()

    override fun resolveReferenceInService(serviceClass: UClass) = serviceClass.sourcePsi

}