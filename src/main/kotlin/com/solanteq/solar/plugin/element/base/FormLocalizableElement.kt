package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject

/**
 * Represents a json element with `name` property that can be localized
 */
abstract class FormLocalizableElement<T : JsonElement>(
    sourceElement: T,
    objectWithNameProperty: JsonObject
) : FormNamedElement<T>(sourceElement, objectWithNameProperty) {

    /**
     * Localization strings for this element.
     * Usually there are two localizations must be present: ru-RU and en-US.
     */
    abstract val localizations: List<String>

}