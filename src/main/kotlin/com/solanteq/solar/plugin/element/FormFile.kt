package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.util.valueAsString

/**
 * Represents a form file (not included)
 */
class FormFile(
    sourceElement: JsonFile,
    private val topLevelObject: JsonObject
) : FormElement<JsonFile>(sourceElement), FormLocalizableElement {

    val nameProperty by lazy { topLevelObject.findProperty("name") }

    val namePropertyValue by lazy { nameProperty?.value as? JsonStringLiteral }

    override val name by lazy { nameProperty.valueAsString() }

    val moduleProperty by lazy { topLevelObject.findProperty("module") }

    val module by lazy { moduleProperty.valueAsString() }

    val fullName by lazy {
        val name = name ?: return@lazy null
        val module = module ?: return@lazy name
        return@lazy "$module.$name"
    }

    val groupRows by lazy {
        topLevelObject.findProperty(FormGroupRow.ARRAY_NAME).toFormArrayElement<FormGroupRow>()
    }

    val groups by lazy {
        topLevelObject.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    /**
     * All groups that are contained in this form.
     * - If groups are represented as `groupRows` property, it will retrieve all inner groups and combine them into this list.
     * - If groups are represented as `groups` property, it will just use them.
     *
     * Never returns null, only empty list.
     */
    val allGroups by lazy {
        val groupRows = groupRows ?: return@lazy groups?.contents ?: return@lazy emptyList()
        val notNullGroups = groupRows.mapNotNull { it.groups }
        return@lazy notNullGroups.flatMap { it.contents }
    }

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

    companion object {

        fun create(sourceElement: JsonElement): FormFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
            if(jsonFile.fileType == TopLevelFormFileType) {
                return FormFile(jsonFile, topLevelObject)
            }
            return null
        }

    }

}