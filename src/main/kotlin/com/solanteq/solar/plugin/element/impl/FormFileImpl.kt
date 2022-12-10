package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.*
import com.solanteq.solar.plugin.element.base.impl.FormLocalizableElementImpl
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.util.valueAsString

class FormFileImpl(
    sourceElement: JsonFile,
    private val topLevelObject: JsonObject
) : FormLocalizableElementImpl<JsonFile>(sourceElement, topLevelObject), FormFile {

    override val moduleProperty by lazy { topLevelObject.findProperty("module") }

    override val module by lazy { moduleProperty.valueAsString() }

    override val fullName by lazy {
        val name = name ?: return@lazy null
        val module = module ?: return@lazy name
        return@lazy "$module.$name"
    }

    override val groupRows by lazy {
        topLevelObject.findProperty(FormGroupRow.ARRAY_NAME).toFormArrayElement<FormGroupRow>()
    }

    override val groups by lazy {
        topLevelObject.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    override val allGroups by lazy {
        val groupRows = groupRows ?: return@lazy groups?.contents ?: return@lazy emptyList()
        val notNullGroups = groupRows.mapNotNull { it.groups }
        return@lazy notNullGroups.flatMap { it.contents }
    }

    override val requests by lazy {
        listOfNotNull(
            sourceRequest,
            saveRequest,
            removeRequest,
            createSourceRequest
        )
    }

    override val sourceRequest by lazy { getRequestByType(FormRequest.RequestType.SOURCE) }
    override val saveRequest by lazy { getRequestByType(FormRequest.RequestType.SAVE) }
    override val removeRequest by lazy { getRequestByType(FormRequest.RequestType.REMOVE) }
    override val createSourceRequest by lazy { getRequestByType(FormRequest.RequestType.CREATE_SOURCE) }

    /**
     * Gets the request by its type, or null if such request isn't present
     */
    private fun getRequestByType(type: FormRequest.RequestType): FormRequest? =
        topLevelObject.findProperty(type.requestLiteral).toFormElement()

    companion object {

        fun create(sourceElement: JsonElement): FormFileImpl? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
            if(jsonFile.fileType == TopLevelFormFileType) {
                return FormFileImpl(jsonFile, topLevelObject)
            }
            return null
        }

    }

}