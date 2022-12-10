package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement

/**
 * A single object inside `groups` array in form or [FormGroupRow] element
 */
interface FormGroup : FormLocalizableElement<JsonObject> {

    val rows: FormPropertyArrayElement<FormRow>?

    companion object {

        const val ARRAY_NAME = "groups"

    }

}