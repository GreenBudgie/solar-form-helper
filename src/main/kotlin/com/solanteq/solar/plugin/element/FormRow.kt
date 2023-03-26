package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.util.FormPsiUtils

/**
 * A single object inside `rows` array in [FormGroup] element
 */
class FormRow(
    sourceElement: JsonObject
) : FormElement<JsonObject>(sourceElement) {

    /**
     * All group that contain this row.
     *
     * Multiple containing groups can exist if this row is in included form
     */
    val containingGroups: List<FormGroup> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormPsiUtils.firstParentsOfType(sourceElement, JsonObject::class).mapNotNull {
            it.toFormElement()
        }
    }

    val fields by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.findProperty(FormField.ARRAY_NAME).toFormArrayElement<FormField>()
    }

    companion object : FormElementCreator<FormRow> {

        const val ARRAY_NAME = "rows"

        override fun create(sourceElement: JsonElement): FormRow? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormRow(sourceElement as JsonObject)
            }
            return null
        }

    }

}