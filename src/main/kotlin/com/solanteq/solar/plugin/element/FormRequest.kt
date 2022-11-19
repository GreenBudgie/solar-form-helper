package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiClass
import com.intellij.psi.search.PsiShortNamesCache
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.evaluateToString
import com.solanteq.solar.plugin.util.findAllCallableServicesImpl
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

/**
 * Represents request definitions in forms
 *
 * Examples:
 * ```
 * "request": "test.testService.find"
 *
 * "save": {
 *   "name": "test.testService.save",
 *   "group": "test"
 * }
 *
 * "source": {
 *   "name": "test.testService.save",
 *   "group": "test"
 *   "params": [
 *     {
 *       "name": "id",
 *       "value": "id"
 *     }
 *   ]
 * }
 * ```
 */
//TODO fun -> lazy fields?
class FormRequest(
    sourceElement: JsonProperty
) : FormElement<JsonProperty>(sourceElement) {

    /**
     * Whether this request has inline notation
     *
     * Examples:
     * ```
     * "request": "test.testService.test" //Inline notation
     *
     * "request": {
     *   "name": "test.testService.test" //Not inline notation
     * }
     * ```
     */
    fun isInline() = sourceElement.value is JsonStringLiteral

    /**
     * Returns request string, or null if there is no request string.
     * This method only returns the text after "name" literal (if it's not inline)
     * or after request literal (if it's inline), so returned string might be invalid in terms of request pattern.
     *
     * If you need a parsed request, use [getRequestData]
     *
     * @see isInline
     */
    fun getRequestString(): String? {
        if(isInline()) {
            val stringLiteral = sourceElement.value as? JsonStringLiteral ?: return null
            return stringLiteral.value
        }
        val jsonObject = sourceElement.value as? JsonObject ?: return null
        val requestNameElement = jsonObject.propertyList.find { it.name == "name" } ?: return null
        val requestNameValue = requestNameElement.value as? JsonStringLiteral ?: return null
        return requestNameValue.value
    }

    /**
     * Parses the request string and returns the valid data,
     * or null if there is no request string or request is invalid
     */
    fun getRequestData(): RequestData? {
        val requestString = getRequestString() ?: return null
        return parseRequestString(requestString)
    }

    fun isRequestValid() = getRequestData() != null

    fun findMethodFromRequest(): UMethod? {
        val methodName = getRequestData()?.methodName ?: return null
        val service = findServiceFromRequest() ?: return null
        return service.methods.find { it.name == methodName }
    }

    fun findServiceFromRequest(): UClass? {
        tryFindServiceByConventionalName()?.let { return it }

        return tryFindServiceByAnnotation()
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
        val requestData = getRequestData() ?: return null

        val exactServiceName =
            requestData.serviceName.replaceFirstChar { it.uppercaseChar() } + "Impl"

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        val applicableServiceClasses = PsiShortNamesCache.getInstance(sourceElement.project).getClassesByName(
            exactServiceName,
            sourceElement.project.allScope()
        )

        if(applicableServiceClasses.isNotEmpty()) {
            val foundService = findApplicableService(applicableServiceClasses, groupDotServiceName)
            if (foundService != null) return foundService
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
        val requestData = getRequestData() ?: return null

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
     * Parses the given request string and returns its data, or null if request string has invalid format
     */
    private fun parseRequestString(requestString: String): RequestData? {
        val requestSplit = requestString.split(".")
        if(requestSplit.size != 3 || requestSplit.any { it.isEmpty() }) return null
        val (groupName, serviceName, methodName) = requestSplit
        return RequestData(groupName, serviceName, methodName)
    }

    data class RequestData(
        val groupName: String,
        val serviceName: String,
        val methodName: String
    )

    enum class RequestType(
        val requestLiteral: String,
        val isInlineRequest: Boolean
    ) {

        SOURCE("source", false),
        SAVE("save", false),
        REMOVE("remove", false),
        CREATE_SOURCE("createSource", false),
        INLINE_REQUEST("request", true),
        INLINE_COUNT_REQUEST("countRequest", true);

        companion object {

            val requestLiterals = values().map { it.requestLiteral }.toTypedArray()
            val formRequests = values().filter { !it.isInlineRequest }.toTypedArray()
            val inlineRequests = values().filter { it.isInlineRequest }.toTypedArray()

        }

    }

}