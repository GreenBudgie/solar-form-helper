package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.creator.FormArrayElementCreator
import com.solanteq.solar.plugin.element.expression.ExpressionAware
import com.solanteq.solar.plugin.element.expression.ExpressionAwareImpl

/**
 * A single object inside `groupRows` array in form
 */
class FormGroupRow(
    sourceElement: JsonObject
) : AbstractFormElement<JsonObject>(sourceElement), ExpressionAware by ExpressionAwareImpl(sourceElement) {

    val groups by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val groupsProperty = sourceElement.findProperty(FormGroup.getArrayName())
        FormGroup.createElementListFrom(groupsProperty)
    }

    companion object : FormArrayElementCreator<FormGroupRow>() {

        override fun getArrayName() = "groupRows"

        override fun createUnsafeFrom(sourceElement: JsonObject) = FormGroupRow(sourceElement)

    }

}