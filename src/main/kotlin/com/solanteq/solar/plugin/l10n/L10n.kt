package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral

/**
 * Represents a plain l10n key-value pair in l10n file
 */
open class L10n(
    val keyElement: JsonStringLiteral,
    val valueElement: JsonStringLiteral
) {

    val key = keyElement.value
    val value = valueElement.value

    override fun hashCode(): Int {
        return 31 * key.hashCode() + value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other !is L10n) return false
        return key == other.key && value == other.value
    }

    companion object {

        /**
         * Creates a l10n object based on the given [JsonProperty] or returns null if it is impossible.
         * Note that this method does not check whether this property belongs to l10n file,
         * you need to do it beforehand.
         */
        fun fromElement(property: JsonProperty): L10n? {
            val keyElement = property.nameElement as? JsonStringLiteral ?: return null
            val valueElement = property.value as? JsonStringLiteral ?: return null
            return L10n(keyElement, valueElement)
        }

    }

}