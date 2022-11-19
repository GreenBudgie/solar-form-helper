package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.util.valueAsString

/**
 * Represents a form file (not included)
 */
class FormFile(
    sourceElement: JsonFile,
    private val topLevelObject: JsonObject
) : FormElement<JsonFile>(sourceElement) {

    fun getFullName(): String? {
        val name = getName() ?: return null
        val module = getModule() ?: return name
        return "$module.$name"
    }

    fun getModule() = topLevelObject.findProperty("module").valueAsString()

    fun getName() = topLevelObject.findProperty("name").valueAsString()

    /**
     * Gets list of all requests in this form. Possible requests are:
     * - source
     * - remove
     * - save
     * - createSource
     */
    fun getRequests(): List<FormRequest> =
        FormRequest.RequestType.formRequests.mapNotNull {
            topLevelObject.findProperty(it.requestLiteral).toFormElement()
        }

    /**
     * Gets the request by its type, or null if such request isn't present
     */
    fun getRequestByType(type: FormRequest.RequestType): FormRequest? =
        topLevelObject.findProperty(type.requestLiteral).toFormElement()

}