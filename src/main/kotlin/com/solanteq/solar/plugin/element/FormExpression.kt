package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormNamedElement
import com.solanteq.solar.plugin.element.creator.FormArrayElementCreator
import com.solanteq.solar.plugin.util.valueAsStringOrNull

/**
 * A single object inside `expressions` array.
 */
class FormExpression(
    sourceElement: JsonObject
) : FormNamedElement<JsonObject>(sourceElement, sourceElement) {

    val valueProperty by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.findProperty("value")
    }

    val valueText by lazy(LazyThreadSafetyMode.PUBLICATION) {
        valueProperty.valueAsStringOrNull()
    }

    companion object : FormArrayElementCreator<FormExpression>() {

        /**
         * Names of properties that may contain an expression as its value
         */
        val expressionProperties = arrayOf(
            "requiredWhen",
            "visibleWhen",
            "editableWhen",
            "removableWhen",
            "editModeWhen",
            "success",
            "warning",
            "error",
            "info",
            "muted"
        )

        override fun getArrayName() = "expressions"

        override fun createUnsafeFrom(sourceElement: JsonObject) = FormExpression(sourceElement)

    }

}