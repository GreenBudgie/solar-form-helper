package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject

abstract class FormObjectInArrayElement(
    sourceElement: JsonObject
) : FormElement<JsonObject>(sourceElement)