package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement

/**
 * Represents a json element with `name` property that can be localized
 */
interface FormLocalizableElement<T : JsonElement> : FormNamedElement<T>