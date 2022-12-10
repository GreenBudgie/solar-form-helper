package com.solanteq.solar.plugin.element.base.impl

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement

abstract class FormElementImpl<T : JsonElement> protected constructor(
    override val sourceElement: T
): FormElement<T> {

    override fun equals(other: Any?): Boolean {
        if(other is FormElementImpl<*>) return other.sourceElement == sourceElement
        return false
    }

    override fun hashCode(): Int {
        return sourceElement.hashCode()
    }

    companion object {

        @JvmStatic
        protected fun canBeCreatedAsArrayElement(
            sourceElement: JsonElement,
            requiredArrayName: String
        ): Boolean {
            val jsonObject = sourceElement as? JsonObject ?: return false
            val parentArray = jsonObject.parent as? JsonArray ?: return false
            val fieldProperty = parentArray.parent as? JsonProperty ?: return false
            return fieldProperty.name == requiredArrayName
        }

    }

}