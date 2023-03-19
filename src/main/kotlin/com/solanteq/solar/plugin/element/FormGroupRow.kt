package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormElement

/**
 * A single object inside `groupRows` array in form
 */
class FormGroupRow(
    sourceElement: JsonObject
) : FormElement<JsonObject>(sourceElement) {

    val groups by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    companion object : FormElementCreator<FormGroupRow> {

        const val ARRAY_NAME = "groupRows"

        override fun create(sourceElement: JsonElement): FormGroupRow? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormGroupRow(sourceElement as JsonObject)
            }
            return null
        }

    }

}