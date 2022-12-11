package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.util.findFormByFullName
import com.solanteq.solar.plugin.util.valueAsString
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Represents an inline configuration property in [FormGroup]
 */
class FormInline(
    sourceElement: JsonProperty,
    private val valueObject: JsonObject
) : FormLocalizableElement<JsonProperty>(sourceElement, valueObject) {

    val request by lazy {
        valueObject.findProperty("request").toFormElement<FormRequest>()
    }

    val formFile by lazy {
        val formProperty = valueObject.findProperty("form") ?: return@lazy null
        val stringValue = formProperty.valueAsString() ?: return@lazy null
        return@lazy findFormByFullName(project, stringValue)?.toPsiFile(project) as? JsonFile
    }

    val formElement by lazy {
        formFile.toFormElement<FormTopLevelFile>()
    }

    companion object : FormElementCreator<FormInline> {

        override fun create(sourceElement: JsonElement): FormInline? {
            val jsonProperty = sourceElement as? JsonProperty ?: return null
            if(jsonProperty.name == "inline") {
                val valueObject = jsonProperty.value as? JsonObject ?: return null
                return FormInline(jsonProperty, valueObject)
            }
            return null
        }

    }

}