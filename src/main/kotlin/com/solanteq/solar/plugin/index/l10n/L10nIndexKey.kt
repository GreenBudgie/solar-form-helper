package com.solanteq.solar.plugin.index.l10n

import com.solanteq.solar.plugin.l10n.L10nLocale

data class L10nIndexKey(
    val key: String,
    val locale: L10nLocale
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as L10nIndexKey

        if (key != other.key) return false
        if (locale != other.locale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + locale.directoryName.hashCode()
        return result
    }

}