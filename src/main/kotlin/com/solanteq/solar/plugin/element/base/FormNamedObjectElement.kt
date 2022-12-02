package com.solanteq.solar.plugin.element.base

import com.solanteq.solar.plugin.element.toFormElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.util.valueAsString

/**
 * Represents a json object element with `name` property.
 *
 * This object **must** be returned via [toFormElement] even if [sourceElement] does not have a `name` property
 * or this property has non-string value. In this case, [name] will return `null`.
 *
 * Example:
 * ```
 * "arrayKey": [
 *   { //Element start
 *     "name": "elementName",
 *     "a": 2
 *   } //Element end
 * ]
 * ```
 *
 * @see FormObjectElement
 */
abstract class FormNamedObjectElement(
    sourceElement: JsonObject,
) : FormObjectElement(sourceElement) {

    /**
     * Name of this form object.
     *
     * It might return null if [sourceElement] does not have a `name` property or
     * this property has non-string value.
     */
    val name by lazy {
        sourceElement.findProperty("name").valueAsString()
    }

}