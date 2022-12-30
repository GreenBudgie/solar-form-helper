package com.solanteq.solar.plugin.util

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange

/**
 * If the value of json property is string literal, returns its text, null otherwise
 */
fun JsonProperty?.valueAsString(): String? {
    val stringLiteral = this?.value as? JsonStringLiteral ?: return null
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