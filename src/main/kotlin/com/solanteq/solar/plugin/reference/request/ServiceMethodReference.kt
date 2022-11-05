package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass

class ServiceMethodReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestData: RequestData
) : AbstractServiceReference(element, range, requestData) {

    override fun resolveReference(serviceClass: UClass) =
        serviceClass.methods.find { it.name == requestData.methodName }?.sourcePsi

}