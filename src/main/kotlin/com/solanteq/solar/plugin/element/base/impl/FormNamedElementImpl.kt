package com.solanteq.solar.plugin.element.base.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.solanteq.solar.plugin.element.base.FormNamedElement
import com.solanteq.solar.plugin.util.valueAsString

abstract class FormNamedElementImpl<T : JsonElement>(
    sourceElement: T,
    private val objectWithNameProperty: JsonObject
) : FormElementImpl<T>(sourceElement), FormNamedElement<T> {

    override val nameProperty by lazy {
        objectWithNameProperty.findProperty("name")
    }

    override val namePropertyValue by lazy {
        nameProperty?.value as? JsonStringLiteral
    }

    override val name by lazy {
        nameProperty.valueAsString()
    }

}