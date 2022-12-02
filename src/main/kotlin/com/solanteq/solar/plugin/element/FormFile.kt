package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.util.valueAsString

/**
 * Represents a form file (not included)
 */
class FormFile(
    sourceElement: JsonFile,
    private val topLevelObject: JsonObject
) : FormElement<JsonFile>(sourceElement), FormLocalizableElement {

    override val name by lazy { topLevelObject.findProperty("name").valueAsString() }

    val fullName by lazy {
        val name = name ?: return@lazy null
        val module = module ?: return@lazy name
        return@lazy "$module.$name"
    }

    val module by lazy { topLevelObject.findProperty("module").valueAsString() }

    /**
     * List of all requests in this form. Possible requests are:
     * - source
     * - save
     * - remove
     * - edit
     * - createSource
     */
    val requests by lazy {
        listOf(
            sourceRequest,
            saveRequest,
            removeRequest,
            createSourceRequest
        )
    }

    val sourceRequest by lazy { getRequestByType(FormRequest.RequestType.SOURCE) }
    val saveRequest by lazy { getRequestByType(FormRequest.RequestType.SAVE) }
    val removeRequest by lazy { getRequestByType(FormRequest.RequestType.REMOVE) }
    val createSourceRequest by lazy { getRequestByType(FormRequest.RequestType.CREATE_SOURCE) }

    /**
     * Gets the request by its type, or null if such request isn't present
     */
    private fun getRequestByType(type: FormRequest.RequestType): FormRequest? =
        topLevelObject.findProperty(type.requestLiteral).toFormElement()

}