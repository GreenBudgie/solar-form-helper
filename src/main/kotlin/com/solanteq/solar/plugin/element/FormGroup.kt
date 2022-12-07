package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.base.FormNamedObjectElement

class FormGroup(
    sourceElement: JsonObject
) : FormNamedObjectElement(sourceElement), FormLocalizableElement {

    val rows by lazy {
        sourceElement.findProperty(FormRow.ARRAY_NAME).toFormArrayElement<FormRow>()
    }

    companion object {

        const val ARRAY_NAME = "groups"

        fun create(sourceElement: JsonElement): FormGroup? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormGroup(sourceElement as JsonObject)
            }
            return null
        }

    }

}