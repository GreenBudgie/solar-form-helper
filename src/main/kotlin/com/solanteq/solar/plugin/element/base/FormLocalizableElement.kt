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
     * Usually there are two localizations must be present: ru-RU and en-US,
     * Actually there might be more duplicate localizations,
     * which is wrong and should be avoided.
     */
    abstract val localizations: List<String>

}