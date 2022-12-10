package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement

/**
 * A single object inside `groupRows` array in form
 */
interface FormGroupRow : FormElement<JsonObject> {

    val groups: FormPropertyArrayElement<FormGroup>?

    companion object {

        const val ARRAY_NAME = "groupRows"

    }

}