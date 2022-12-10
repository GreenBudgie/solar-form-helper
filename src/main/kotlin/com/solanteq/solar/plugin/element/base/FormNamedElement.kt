package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.solanteq.solar.plugin.element.toFormElement
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral

/**
 * Represents a json element with `name` property.
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
 */
interface FormNamedElement<T : JsonElement> : FormElement<T> {

    /**
     * An actual json property element that represents name of this form
     */
    val nameProperty: JsonProperty?

    /**
     * Name of this form object.
     *
     * It might return null if [sourceElement] does not have a `name` property or
     * this property has non-string value.
     */
    val name: String?

    /**
     * An actual json property value element that represents name of this form
     */
    val namePropertyValue: JsonStringLiteral?

}