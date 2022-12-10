package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.element.FormRow
import com.solanteq.solar.plugin.element.base.impl.FormLocalizableElementImpl
import com.solanteq.solar.plugin.element.toFormArrayElement

class FormGroupImpl(
    sourceElement: JsonObject
) : FormLocalizableElementImpl<JsonObject>(sourceElement, sourceElement), FormGroup {

    override val rows by lazy {
        sourceElement.findProperty(FormRow.ARRAY_NAME).toFormArrayElement<FormRow>()
    }

    companion object {

        fun create(sourceElement: JsonElement): FormGroupImpl? {
            if(canBeCreatedAsArrayElement(sourceElement, FormGroup.ARRAY_NAME)) {
                return FormGroupImpl(sourceElement as JsonObject)
            }
            return null
        }

    }

}