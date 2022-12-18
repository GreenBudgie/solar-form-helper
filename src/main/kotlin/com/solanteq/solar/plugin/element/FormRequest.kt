package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.CachedValue
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.search.CallableServiceSearch
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.evaluateToString
import org.jetbrains.kotlin.idea.base.util.allScope
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
    val isInline by lazy { sourceElement.value is JsonStringLiteral }

    /**
     * Returns request string, or null if there is no request string.
     * This property only returns the text after "name" literal (if it's not inline)
     * or after request literal (if it's inline), so returned string might be invalid in terms of request pattern.
     *
     * If you need a parsed request, use [requestData]
     *
     * @see isInline
     */
    val requestString by lazy {
        if(isInline) {
            val stringLiteral = sourceElement.value as? JsonStringLiteral ?: return@lazy null
            return@lazy stringLiteral.value
        }
        val jsonObject = sourceElement.value as? JsonObject ?: return@lazy null
        val requestNameElement = jsonObject.propertyList.find { it.name == "name" } ?: return@lazy null
        val requestNameValue = requestNameElement.value as? JsonStringLiteral ?: return@lazy null
        return@lazy requestNameValue.value
    }

    /**
     * Parses the request string and returns the valid data,
     * or null if there is no request string or request is invalid
     */
    val requestData by lazy {
        val requestString = requestString ?: return@lazy null
        return@lazy parseRequestString(requestString)
    }

    /**
     * Whether this request has a valid request data (contains service and method names)
     */
    val isRequestValid by lazy { requestData != null }

    /**
     * Returns UAST method to which the request points to,
     * or null if request is invalid or no method/service is found
     */
    val methodFromRequest: UMethod? by lazy {
        val methodName = requestData?.methodName ?: return@lazy null
        val service = serviceFromRequest ?: return@lazy null
        return@lazy service.allMethods.find { it.name == methodName }.toUElementOfType()
    }

    /**
     * Returns UAST service to which the request points to,
     * or null if request is invalid or no service is found
     */
    val serviceFromRequest by lazy {
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

        val applicableServiceClasses = PsiShortNamesCache.getInstance(project).getClassesByName(
            exactServiceName,
            project.allScope()
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

        val allServices = CallableServiceSearch.findAllCallableServicesImpl(project).toTypedArray()

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
        val isFormRequest: Boolean
    ) {

        SOURCE("source", true),
        SAVE("save", true),
        REMOVE("remove", true),
        CREATE_SOURCE("createSource", true),
        INLINE_REQUEST("request", false),
        INLINE_COUNT_REQUEST("countRequest", false);

        companion object {

            val requestLiterals = values().map { it.requestLiteral }.toTypedArray()
            val formRequests = values().filter { it.isFormRequest }.toTypedArray()

        }

    }

    companion object : FormElementCreator<FormRequest> {

        override val key = Key<CachedValue<FormRequest>>("solar.element.request")

        override fun create(sourceElement: JsonElement): FormRequest? {
            val jsonProperty = sourceElement as? JsonProperty ?: return null
            if(jsonProperty.name in RequestType.requestLiterals) {
                return FormRequest(jsonProperty)
            }
            return null
        }

    }

}