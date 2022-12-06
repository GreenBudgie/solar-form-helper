package com.solanteq.solar.plugin.util

import com.intellij.json.psi.*

/**
 * If the value of json property is string literal, returns its text, null otherwise
 */
fun JsonProperty?.valueAsString(): String? {
    val stringLiteral = this?.value as? JsonStringLiteral ?: return null
    return stringLiteral.value
}