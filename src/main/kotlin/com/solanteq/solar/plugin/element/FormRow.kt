package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormNamedElement

/**
 * A single object inside `rows` array in [FormGroup] element
 */
class FormRow(
    sourceElement: JsonObject
) : FormNamedElement<JsonObject>(sourceElement, sourceElement) {

    val fields by lazy {
        sourceElement.findProperty(FormField.ARRAY_NAME).toFormArrayElement<FormField>()
    }

    companion object {

        const val ARRAY_NAME = "rows"

        fun create(sourceElement: JsonElement): FormRow? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormRow(sourceElement as JsonObject)
            }
            return null
        }

    }

}