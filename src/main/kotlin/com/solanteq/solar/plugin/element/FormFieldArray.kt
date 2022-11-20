package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty

class FormFieldArray(
    sourceElement: JsonProperty,
    valueArray: JsonArray
) : FormPropertyArrayOfObjectsElement<FormField>(sourceElement, valueArray, FormField::class) {



}