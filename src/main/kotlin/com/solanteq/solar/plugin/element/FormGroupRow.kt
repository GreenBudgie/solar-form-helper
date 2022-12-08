package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormObjectElement

class FormGroupRow(
    sourceElement: JsonObject
) : FormObjectElement(sourceElement) {

    val groups by lazy {
        sourceElement.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    companion object {

        const val ARRAY_NAME = "groupRows"

        fun create(sourceElement: JsonElement): FormGroupRow? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormGroupRow(sourceElement as JsonObject)
            }
            return null
        }

    }

}