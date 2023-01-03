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

/**
 * Splits the specified string literal by '.' character into pairs of text ranges and strings.
 *
 * Examples:
 * ```
 * "a.bb.ccc".dotSplit() ->
 * [
 *  TextRange(1, 2) to "a"
 *  TextRange(3, 5) to "bb"
 *  TextRange(6, 9) to "ccc"
 * ]
 *
 * "".dotSplit() -> [TextRange(1, 1) to ""]
 *
 * "a.".dotSplit() ->
 * [
 *  TextRange(1, 2) to "a"
 *  TextRange(3, 3) to ""
 * ]
 * ```
 */
fun JsonStringLiteral.dotSplit(): List<Pair<TextRange, String>> {
    val value = value

    val pairs = mutableListOf<Pair<TextRange, String>>()
    var currentPosition = 0
    do {
        val nextDotPosition = value.indexOf(".", startIndex = currentPosition)
        val textRange = if(nextDotPosition == -1) {
            TextRange.create(currentPosition, value.length)
        } else {
            TextRange.create(currentPosition, nextDotPosition)
        }
        val stringInRange = textRange.substring(value)
        val realTextRange = textRange.shiftRight(1)
        pairs += realTextRange to stringInRange

        currentPosition = nextDotPosition + 1
    } while(nextDotPosition != -1)

    return pairs
}