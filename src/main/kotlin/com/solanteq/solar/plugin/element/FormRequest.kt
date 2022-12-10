package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

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
interface FormRequest : FormElement<JsonProperty> {

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
    val isInline: Boolean

    /**
     * Returns request string, or null if there is no request string.
     * This property only returns the text after "name" literal (if it's not inline)
     * or after request literal (if it's inline), so returned string might be invalid in terms of request pattern.
     *
     * If you need a parsed request, use [requestData]
     *
     * @see isInline
     */
    val requestString: String?

    /**
     * Parses the request string and returns the valid data,
     * or null if there is no request string or request is invalid
     */
    val requestData: RequestData?

    /**
     * Whether this request has a valid request data (contains service and method names)
     */
    val isRequestValid: Boolean?

    /**
     * Returns UAST method to which the request points to,
     * or null if request is invalid or no method/service is found
     */
    val methodFromRequest: UMethod?

    /**
     * Returns UAST service to which the request points to,
     * or null if request is invalid or no service is found
     */
    val serviceFromRequest: UClass?

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

}