package com.solanteq.solar.plugin.util

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange

/**
 * Utility class that helps to split a string into chain of [TextRange]s and corresponding text
 * by delimiter character (dot by default).
 *
 * Can be also referred as `chain` in some parts of code.
 *
 * Examples:
 * ```
 * "a.bb.ccc" ->
 * [
 *  TextRange(0, 1) to "a"
 *  TextRange(2, 4) to "bb"
 *  TextRange(5, 8) to "ccc"
 * ]
 *
 * "" -> [TextRange(0, 0) to ""]
 *
 * "a." ->
 * [
 *  TextRange(0, 1) to "a"
 *  TextRange(2, 2) to ""
 * ]
 * ```
 */
class RangeSplit(
    entries: List<RangeSplitEntry>
) : List<RangeSplitEntry> by entries {

    val ranges = entries.map { it.range }
    val strings = entries.map { it.text }

    /**
     * Shifts right all [TextRange]s in this split by specified delta and returns the new [RangeSplit]
     * @see TextRange.shiftRight
     */
    fun shiftedRight(delta: Int): RangeSplit {
        val newEntries = map {
            RangeSplitEntry(
                it.range.shiftRight(delta),
                it.text
            )
        }
        return RangeSplit(newEntries)
    }

    companion object {

        /**
         * Splits value of the specified [JsonStringLiteral] into chain of [TextRange]s
         * and corresponding text by delimiter character (dot by default).
         *
         * Returns real [TextRange] positions in [JsonStringLiteral] (shifted right by 1).
         */
        fun from(stringLiteral: JsonStringLiteral, delimiter: Char = '.'): RangeSplit {
            return from(stringLiteral.value, delimiter).shiftedRight(1)
        }

        /**
         * Splits a string into chain of [TextRange]s and corresponding text
         * by delimiter character (dot by default).
         */
        fun from(string: String, delimiter: Char = '.'): RangeSplit {
            val entries = mutableListOf<RangeSplitEntry>()
            var currentPosition = 0
            do {
                val nextDelimiterPosition = string.indexOf(delimiter, startIndex = currentPosition)
                val textRange = if(nextDelimiterPosition == -1) {
                    TextRange.create(currentPosition, string.length)
                } else {
                    TextRange.create(currentPosition, nextDelimiterPosition)
                }
                val stringInRange = textRange.substring(string)
                entries += RangeSplitEntry(textRange, stringInRange)

                currentPosition = nextDelimiterPosition + 1
            } while(nextDelimiterPosition != -1)

            return RangeSplit(entries)
        }

        fun empty() = RangeSplit(emptyList())

    }

}

data class RangeSplitEntry(
    val range: TextRange,
    val text: String
)

/**
 * Converts a list of [RangeSplitEntry]ies back to [RangeSplit]
 */
fun List<RangeSplitEntry>.convert() = RangeSplit(this)