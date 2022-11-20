package com.solanteq.solar.plugin.element

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
abstract class FormPropertyObjectElement(
    sourceElement: JsonProperty,
    val valueObject: JsonObject
) : FormElement<JsonProperty>(sourceElement)