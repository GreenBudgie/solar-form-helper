package com.solanteq.solar.plugin.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes

class InvalidRequestInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            val requestElement = FormRequest.createFrom(property) ?: return
            val element = requestElement.requestStringElement ?: return
            if (!requestElement.isValid) {
                holder.registerProblem(
                    element,
                    "Request has invalid format",
                    ProblemHighlightType.ERROR,
                    element.textRangeWithoutQuotes
                )
                return
            }
            val referencedService = requestElement.referencedService ?: return
            val requestData = requestElement.requestData
                ?: error("Request was considered valid, but requestData is null")
            val serviceNameData = requestData.service
                ?: error("Request was considered valid, but service name is null")
            val serviceName = serviceNameData.text
            if (!referencedService.isCallableServiceClassImpl()) {
                holder.registerProblem(
                    element,
                    "$serviceName is not @Callable",
                    ProblemHighlightType.WARNING,
                    serviceNameData.range
                )
            }
            val referencedMethod = requestElement.referencedMethod
            val methodData = requestData.method ?: error("Request was considered valid, but method is null")
            val methodName = methodData.text
            if (referencedMethod == null) {
                holder.registerProblem(
                    element,
                    "Method \"$methodName\" is not found in $serviceName",
                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                    methodData.range
                )
                return
            }
            val callableMethod = requestElement.referencedCallableMethod
            if (callableMethod == null) {
                holder.registerProblem(
                    element,
                    "Method \"$methodName\" in $serviceName is not @Callable",
                    ProblemHighlightType.WARNING,
                    methodData.range
                )
            }
        }

    }

}