package com.solanteq.solar.plugin.element.base

import com.solanteq.solar.plugin.element.toFormElement
import com.intellij.json.psi.JsonElement

/**
 * Form element is a representation of a SOLAR form json element.
 * Form element must only be created via [toFormElement] call.
 *
 * The main goal of form elements is to provide a pseudo PSI tree for SOLAR forms without
 * actually creating one.
 *
 * It does not inherit [JsonElement], but does contain [sourceElement]
 * as a link to original field it have been created from.
 *
 * [sourceElement] has a particular json element type for which [toFormElement] must be called.
 *
 * **Important note**: *form elements are mostly utility classes and data storage, not PSI elements themselves!*
 *
 * Form elements must not be stored inside any containers or in cache.
 * Most of its properties are lazy-initialized from source element.
 * As it does not inherit [JsonElement], [sourceElement] can become invalid at any time after creation
 * which can break how form element works. Try to only use form elements in-place.
 * If you need to update any information about the element, just reuse [toFormElement] method on json element.
 */
abstract class FormElement<T : JsonElement> protected constructor(
    val sourceElement: T
)