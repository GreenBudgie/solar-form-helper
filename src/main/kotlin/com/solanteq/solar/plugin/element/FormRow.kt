package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormNamedElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement

/**
 * A single object inside `rows` array in [FormGroup] element
 */
interface FormRow : FormNamedElement<JsonObject> {

    val fields: FormPropertyArrayElement<FormField>?

    companion object {

        const val ARRAY_NAME = "rows"

    }

}