package com.solanteq.solar.plugin.element.base.impl

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormPropertyObjectElement

abstract class FormPropertyObjectElementImpl<T : FormElement<JsonObject>>(
    sourceElement: JsonProperty,
    override val valueObject: T
) : FormElementImpl<JsonProperty>(sourceElement), FormPropertyObjectElement<T>