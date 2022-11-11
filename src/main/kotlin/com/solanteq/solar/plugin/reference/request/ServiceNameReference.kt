package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonPsiUtil
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.jetbrains.jsonSchema.settings.mappings.JsonSchemaPatternComparator
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.findAllCallableServicesImpl
import com.solanteq.solar.plugin.util.serviceName
import org.jetbrains.uast.*

class ServiceNameReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestData: RequestData?
) : AbstractServiceReference(element, range, requestData) {

    override fun getVariants(): Array<Any> {
        val applicableServices = findAllCallableServicesImpl(element.project)
        return applicableServices.mapNotNull { it.serviceName }.toTypedArray()
    }

    override fun resolveReferenceInService(serviceClass: UClass) = serviceClass.sourcePsi

}