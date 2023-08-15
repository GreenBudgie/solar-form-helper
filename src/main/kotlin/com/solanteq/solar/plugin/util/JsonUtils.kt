package com.solanteq.solar.plugin.util

import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange

/**
 * If the value of json property is number literal, returns its double value, null otherwise
 */
fun JsonProperty?.valueAsDoubleOrNull(): Double? {
    val numberLiteral = this?.value as? JsonNumberLiteral ?: return null
    return numberLiteral.value
}

/**
 * If the value of json property is number literal, returns its integer value (rounding down), null otherwise
 */
fun JsonProperty?.valueAsIntOrNull() = valueAsDoubleOrNull()?.toInt()

/**
 * If the value of json property is string literal, returns its text, null otherwise
 */
fun JsonProperty?.valueAsStringOrNull(): String? {
    val stringLiteral = this?.value as? JsonStringLiteral ?: return null
    return stringLiteral.value
}

/**
 * If the value of json property is string literal, returns its text, empty string otherwise
 */
fun JsonProperty?.valueAsStringOrEmpty(): String {
    val stringLiteral = this?.value as? JsonStringLiteral ?: return ""
    return stringLiteral.value
}

/**
 * Relative text range of this string literal with trimmed quotes
 */
val JsonStringLiteral.textRangeWithoutQuotes: TextRange
    get() {
        val value = value
        if(value.isEmpty()) return TextRange(0, 0)
        return TextRange.from(1, value.length)
    }