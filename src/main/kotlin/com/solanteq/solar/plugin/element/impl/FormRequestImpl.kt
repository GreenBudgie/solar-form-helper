package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiClass
import com.intellij.psi.search.PsiShortNamesCache
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.element.base.impl.FormElementImpl
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.evaluateToString
import com.solanteq.solar.plugin.util.findAllCallableServicesImpl
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

class FormRequestImpl(
    sourceElement: JsonProperty
) : FormElementImpl<JsonProperty>(sourceElement), FormRequest {

    override val isInline by lazy { sourceElement.value is JsonStringLiteral }

    override val requestString by lazy {
        if(isInline) {
            val stringLiteral = sourceElement.value as? JsonStringLiteral ?: return@lazy null
            return@lazy stringLiteral.value
        }
        val jsonObject = sourceElement.value as? JsonObject ?: return@lazy null
        val requestNameElement = jsonObject.propertyList.find { it.name == "name" } ?: return@lazy null
        val requestNameValue = requestNameElement.value as? JsonStringLiteral ?: return@lazy null
        return@lazy requestNameValue.value
    }

    override val requestData by lazy {
        val requestString = requestString ?: return@lazy null
        return@lazy parseRequestString(requestString)
    }

    override val isRequestValid by lazy { requestData != null }

    override val methodFromRequest: UMethod? by lazy {
        val methodName = requestData?.methodName ?: return@lazy null
        val service = serviceFromRequest ?: return@lazy null
        return@lazy service.allMethods.find { it.name == methodName }.toUElementOfType()
    }

    override val serviceFromRequest by lazy {
        tryFindServiceByConventionalName()?.let { return@lazy it }

        return@lazy tryFindServiceByAnnotation()
    }

    /**
     * A fast way to search for applicable service.
     *
     * This method tries to find a service by conventional SOLAR service naming:
     * ```
     * "test.testService" -> TestServiceImpl
     * ```
     * Not all SOLAR services follow this naming rule, so slow method might be used afterward.
     * No cache is used.
     */
    private fun tryFindServiceByConventionalName(): UClass? {
        val requestData = requestData ?: return null

        val exactServiceName =
            requestData.serviceName.replaceFirstChar { it.uppercaseChar() } + "Impl"

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        val applicableServiceClasses = PsiShortNamesCache.getInstance(sourceElement.project).getClassesByName(
            exactServiceName,
            sourceElement.project.allScope()
        )

        if(applicableServiceClasses.isNotEmpty()) {
            findApplicableService(applicableServiceClasses, groupDotServiceName)?.let { return it }
        }

        return null
    }

    /**
     * A slower way to search for applicable service. Used when fast method has failed.
     *
     * This method searches for every @Service annotation usage and finds a service by its value.
     * Uses caching.
     */
    private fun tryFindServiceByAnnotation(): UClass? {
        val requestData = requestData ?: return null

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        val allServices = findAllCallableServicesImpl(sourceElement.project).toTypedArray()

        return findApplicableService(allServices, groupDotServiceName)
    }

    private fun findApplicableService(services: Array<PsiClass>, serviceName: String): UClass? {
        return services.find {
            it
                .getAnnotation(SERVICE_ANNOTATION_FQ_NAME)
                ?.findAttributeValue("value")
                ?.evaluateToString() == serviceName
        }?.toUElementOfType()
    }

    /**
     * Parses the given request string and returns its data,
     * or null if request string has invalid format
     */
    private fun parseRequestString(requestString: String): FormRequest.RequestData? {
        val requestSplit = requestString.split(".")
        if(requestSplit.size != 3 || requestSplit.any { it.isEmpty() }) return null
        val (groupName, serviceName, methodName) = requestSplit
        return FormRequest.RequestData(groupName, serviceName, methodName)
    }

    companion object {

        fun create(sourceElement: JsonElement): FormRequestImpl? {
            val jsonProperty = sourceElement as? JsonProperty ?: return null
            if(jsonProperty.name in FormRequest.RequestType.requestLiterals) {
                return FormRequestImpl(jsonProperty)
            }
            return null
        }

    }

}