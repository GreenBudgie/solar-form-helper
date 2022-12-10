package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormRow
import com.solanteq.solar.plugin.element.base.impl.FormNamedElementImpl
import com.solanteq.solar.plugin.element.toFormArrayElement

class FormRowImpl(
    sourceElement: JsonObject
) : FormNamedElementImpl<JsonObject>(sourceElement, sourceElement), FormRow {

    override val fields by lazy {
        sourceElement.findProperty(FormField.ARRAY_NAME).toFormArrayElement<FormField>()
    }

    companion object {

        fun create(sourceElement: JsonElement): FormRowImpl? {
            if(canBeCreatedAsArrayElement(sourceElement, FormRow.ARRAY_NAME)) {
                return FormRowImpl(sourceElement as JsonObject)
            }
            return null
        }

    }

}