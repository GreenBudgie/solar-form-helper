package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.element.FormRootFile

/**
 * A completely unique l10n entry by which you can also identify the origin of this localization - its root form
 */
data class FormL10nEntry(
    val rootForm: FormRootFile,
    val key: String,
    val locale: L10nLocale
) {

    override fun toString(): String {
        return "$key (${locale.displayName})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormL10nEntry

        if (key != other.key) return false
        if (locale != other.locale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + locale.hashCode()
        return result
    }


    companion object {

        fun withAllLocales(rootForm: FormRootFile, key: String): List<FormL10nEntry> {
            return L10nLocale.entries.map { locale ->
                FormL10nEntry(
                    rootForm = rootForm,
                    key = key,
                    locale = locale
                )
            }
        }

    }

}

fun List<FormL10nEntry>.append(postfix: String): List<FormL10nEntry> = map {
    it.copy(key = it.key + postfix)
}

fun List<FormL10nEntry>.withSameFormAndLocaleAs(otherEntry: FormL10nEntry): List<FormL10nEntry> = filter {
    it.rootForm == otherEntry.rootForm && it.locale == otherEntry.locale
}

fun List<FormL10nEntry>.withLocale(locale: L10nLocale): List<FormL10nEntry> = filter {
    it.locale == locale
}