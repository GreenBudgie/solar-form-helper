package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty

/**
 * Represents a json object element.
 * Usually used as an array entry.
 *
 * Example:
 * ```
 * "arrayKey": [
 *   { //Element start
 *     "a": 1,
 *     "b": 2
 *   } //Element end
 * ]
 * ```
 */
abstract class FormObjectElement(
    sourceElement: JsonObject
) : FormElement<JsonObject>(sourceElement) {

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