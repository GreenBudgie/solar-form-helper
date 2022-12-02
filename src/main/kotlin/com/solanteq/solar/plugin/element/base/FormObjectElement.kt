package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonObject

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
) : FormElement<JsonObject>(sourceElement)