package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty

/**
 * Represents a json property with object value.
 *
 * Example:
 * ```
 * "key": {
 *   "a": 1,
 *   "b": 2
 * }
 * ```
 */
interface FormPropertyObjectElement<T : FormElement<JsonObject>> : FormElement<JsonProperty> {

    val valueObject: T

}