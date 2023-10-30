package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral

/**
 * Represents a plain l10n key-value pair in l10n file with locale
 */
open class L10n(
    val file: JsonFile,
    val property: JsonProperty,
    val keyElement: JsonStringLiteral,
    val valueElement: JsonStringLiteral,
    val locale: L10nLocale
) {

    val key = keyElement.value
    val value = valueElement.value

    override fun equals(other: Any?): Boolean {
        if(other !is L10n) return false
        return key == other.key && value == other.value && locale == other.locale
    }

    override fun hashCode(): Int {
        var result = locale.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    /**
     * Converts this plain l10n into [FormL10n] if it is possible, or returns null
     */
    fun toFormL10n() = FormL10n.fromL10n(this)

    companion object {

        /**
         * Creates a l10n object based on the given [JsonProperty] or returns null if it is impossible.
         * Note that this method does not check whether this property belongs to l10n file,
         * you need to do it beforehand.
         */
        fun fromElement(property: JsonProperty): L10n? {
            val keyElement = property.nameElement as? JsonStringLiteral ?: return null
            val valueElement = property.value as? JsonStringLiteral ?: return null
            val file = property.containingFile?.originalFile as? JsonFile ?: return null
            val parentDirectory = file.parent ?: return null
            val locale = L10nLocale.getByDirectoryName(parentDirectory.name) ?: return null
            return L10n(file, property, keyElement, valueElement, locale)
        }

    }

}