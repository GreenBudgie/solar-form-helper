package com.solanteq.solar.plugin.inspection.form

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.bundle.SolarBundle
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
                    SolarBundle.message("inspection.message.request.has.invalid.format"),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
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
                    SolarBundle.message("inspection.message.request.class.not.callable", serviceName),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    serviceNameData.range
                )
            }
            val referencedMethod = requestElement.referencedMethod
            val methodData = requestData.method ?: error("Request was considered valid, but method is null")
            val methodName = methodData.text
            if (referencedMethod == null) {
                holder.registerProblem(
                    element,
                    SolarBundle.message("inspection.message.request.method.not.found", methodName, serviceName),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    methodData.range
                )
                return
            }
            val callableMethod = requestElement.referencedCallableMethod
            if (callableMethod == null) {
                holder.registerProblem(
                    element,
                    SolarBundle.message("inspection.message.request.method.not.callable", methodName, serviceName),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    methodData.range
                )
            }
        }

    }

}