package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.toFormElement
import kotlin.reflect.KClass

/**
 * Represents a json property with array value that contains form objects
 *
 * Example:
 * ```
 * "array": [
 *   {
 *     "key1": "value1"
 *   },
 *   {
 *     "key2": "value2"
 *   }
 * ]
 * ```
 */
class FormPropertyArrayElement<T : FormObjectElement>(
    sourceElement: JsonProperty,
    val valueArray: JsonArray,
    private val formObjectClass: KClass<out T>
) : FormElement<JsonProperty>(sourceElement) {

    /**
     * Contents of this array represented as form object elements that this array stores.
     * It only returns valid form elements that can be resolved via [toFormElement].
     *
     * TODO also resolve json include
     */
    val contents by lazy {
        valueArray.valueList.mapNotNull { it.toFormElement(formObjectClass) }
    }

}