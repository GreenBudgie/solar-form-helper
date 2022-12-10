package com.solanteq.solar.plugin.element.base.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject

abstract class FormLocalizableElementImpl<T : JsonElement>(
    sourceElement: T,
    objectWithNameProperty: JsonObject
) : FormNamedElementImpl<T>(sourceElement, objectWithNameProperty)