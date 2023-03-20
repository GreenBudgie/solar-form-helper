package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.index.CallableServiceImplIndex
import com.solanteq.solar.plugin.util.serviceSolarName
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.uast.UFile
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
    val isInline by lazy(LazyThreadSafetyMode.PUBLICATION) { sourceElement.value is JsonStringLiteral }

    /**
     * Returns string literal element that represents the request string itself,
     * or null if request string element does exist.
     *
     * Request string looks like `test.testService.method`.
     *
     * @see requestString
     */
    val requestStringElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if (isInline) {
            return@lazy sourceElement.value as? JsonStringLiteral
        }
        val jsonObject = sourceElement.value as? JsonObject ?: return@lazy null
        val requestNameElement = jsonObject.propertyList.find { it.name == "name" } ?: return@lazy null
        return@lazy requestNameElement.value as? JsonStringLiteral
    }

    /**
     * Returns request string, or null if [requestStringElement] is also null.
     * This property only returns the text after "name" literal (when [isInline] is false)
     * or after request literal (when [isInline] is true), so returned string might be invalid
     * in terms of request pattern.
     *
     * If you need a parsed request, consider using [requestData].
     *
     * @see requestStringElement
     */
    val requestString by lazy(LazyThreadSafetyMode.PUBLICATION) {
        return@lazy requestStringElement?.value
    }

    /**
     * Parses the request string and returns the valid data,
     * or null if there is no request string or request is invalid
     */
    val requestData by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val requestString = requestString ?: return@lazy null
        return@lazy parseRequestString(requestString)
    }

    /**
     * Whether this request has a valid request data (contains service and method names)
     */
    val isRequestValid by lazy(LazyThreadSafetyMode.PUBLICATION) { requestData != null }

    /**
     * Returns UAST method to which the request points to,
     * or null if request is invalid or no method/service is found
     */
    val methodFromRequest: PsiMethod? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val methodName = requestData?.methodName ?: return@lazy null
        val service = serviceFromRequest ?: return@lazy null
        return@lazy service.allMethods.find { it.name == methodName }
    }

    /**
     * Returns UAST service to which the request points to,
     * or null if request is invalid or no service is found
     */
    val serviceFromRequest by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val requestData = requestData ?: return@lazy null
        val fullServiceName = "${requestData.groupName}.${requestData.serviceName}"

        val applicableFiles = CallableServiceImplIndex.getFilesContainingCallableServiceImpl(
            fullServiceName, project.allScope()
        ).mapNotNull { it.toPsiFile(project)?.toUElementOfType<UFile>() }
        val possibleServices = applicableFiles.flatMap { it.classes }

        findApplicableService(possibleServices, fullServiceName)
    }

    private fun findApplicableService(possibleServices: Collection<PsiClass>,
                                      requiredServiceName: String): PsiClass? {
        return possibleServices.find {
            it.serviceSolarName == requiredServiceName
        }
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

        override fun create(sourceElement: JsonElement): FormRequest? {
            val jsonProperty = sourceElement as? JsonProperty ?: return null
            if(jsonProperty.name in RequestType.requestLiterals) {
                return FormRequest(jsonProperty)
            }
            return null
        }

    }

}