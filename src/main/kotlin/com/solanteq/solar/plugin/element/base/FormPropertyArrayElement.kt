package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.toFormElement

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
interface FormPropertyArrayElement<T : FormElement<JsonObject>> : FormElement<JsonProperty>, List<T> {

    val valueArray: JsonArray

    /**
     * Contents of this array represented as form object elements that this array stores.
     * It only returns valid form elements that can be resolved via [toFormElement].
     *
     * TODO also resolve json include
     */
    val contents: List<T>

}