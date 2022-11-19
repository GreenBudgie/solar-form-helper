package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.util.valueAsString

/**
 * Represents a form file (not included)
 */
class FormFile(
    sourceElement: JsonFile
) : FormElement<JsonFile>(sourceElement) {

    val topLevelObject by lazy { sourceElement.topLevelValue as? JsonObject }

    val fullName by lazy {
        val name = name ?: return@lazy null
        val module = module ?: return@lazy name
        return@lazy "$module.$name"
    }

    val module by lazy { topLevelObject?.findProperty("module").valueAsString() }

    val name by lazy { topLevelObject?.findProperty("name").valueAsString() }

    /**
     * List of all requests in this form. Possible requests are:
     * - source
     * - remove
     * - save
     * - createSource
     */
    val requests by lazy {
        FormRequest.RequestType.formRequests.mapNotNull {
            topLevelObject?.findProperty(it.requestLiteral).toFormElement()
        }
    }

    /**
     * Gets the request by its type, or null if such request isn't present
     */
    fun getRequestByType(type: FormRequest.RequestType): FormRequest? =
        topLevelObject?.findProperty(type.requestLiteral).toFormElement()

}