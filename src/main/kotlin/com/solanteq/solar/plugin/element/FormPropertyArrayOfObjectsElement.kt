package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty
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
abstract class FormPropertyArrayOfObjectsElement<T : FormObjectInArrayElement>(
    sourceElement: JsonProperty,
    val valueArray: JsonArray,
    private val formObjectClass: KClass<T>
) : FormElement<JsonProperty>(sourceElement) {

    val contents by lazy {
        valueArray.valueList.mapNotNull { it.toFormElement(formObjectClass) }
    }

}