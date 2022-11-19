package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral

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

    companion object {

        /**
         * Parses the given request string and returns its data, or null if request string has invalid format
         */
        fun parseRequestString(requestString: String): RequestData? {
            val requestSplit = requestString.split(".")
            if(requestSplit.size != 3 || requestSplit.any { it.isEmpty() }) return null
            val (groupName, serviceName, methodName) = requestSplit
            return RequestData(groupName, serviceName, methodName)
        }

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