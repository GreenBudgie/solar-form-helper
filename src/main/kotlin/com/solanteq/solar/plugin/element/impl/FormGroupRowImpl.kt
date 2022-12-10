package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.element.base.impl.FormElementImpl
import com.solanteq.solar.plugin.element.toFormArrayElement

class FormGroupRowImpl(
    sourceElement: JsonObject
) : FormElementImpl<JsonObject>(sourceElement), FormGroupRow {

    override val groups by lazy {
        sourceElement.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    companion object {

        fun create(sourceElement: JsonElement): FormGroupRowImpl? {
            if(canBeCreatedAsArrayElement(sourceElement, FormGroupRow.ARRAY_NAME)) {
                return FormGroupRowImpl(sourceElement as JsonObject)
            }
            return null
        }

    }

}