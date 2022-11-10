package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.solanteq.solar.plugin.util.callableMethods
import org.jetbrains.uast.UClass

class ServiceMethodReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestData: RequestData
) : AbstractServiceReference(element, range, requestData) {

    override fun getVariantsInService(serviceClass: UClass): Array<Any> =
        serviceClass.callableMethods.toTypedArray()

    override fun resolveReferenceInService(serviceClass: UClass) =
        serviceClass.methods.find { it.name == requestData.methodName }?.sourcePsi

}