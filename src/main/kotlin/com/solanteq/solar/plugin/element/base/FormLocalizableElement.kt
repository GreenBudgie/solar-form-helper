package com.solanteq.solar.plugin.element.base

/**
 * Represents a json element with `name` property that can be localized.
 *
 * Usually used with [FormNamedObjectElement].
 */
interface FormLocalizableElement {

    /**
     * Name of this element.
     *
     * Similar to [FormNamedObjectElement].
     */
    val name: String?

}