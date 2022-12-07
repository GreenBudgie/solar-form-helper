package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormNamedObjectElement

class FormRow(
    sourceElement: JsonObject
) : FormNamedObjectElement(sourceElement) {

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