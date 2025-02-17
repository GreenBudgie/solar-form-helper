package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.element.FormRootFile

/**
 * A completely unique l10n entry by which you can identify the origin of this localization - its root form
 */
data class L10nEntry(
    val rootForm: FormRootFile,
    val key: String,
    val locale: L10nLocale
) {

    companion object {

        fun withAllLocales(rootForm: FormRootFile, key: String): List<L10nEntry> {
            return L10nLocale.entries.map { locale ->
                L10nEntry(
                    rootForm = rootForm,
                    key = key,
                    locale = locale
                )
            }
        }

    }

}

fun List<L10nEntry>.append(postfix: String): List<L10nEntry> = map {
    it.copy(key = it.key + postfix)
}

fun List<L10nEntry>.withSameFormAndLocaleAs(otherEntry: L10nEntry): L10nEntry? = find {
    it.rootForm == otherEntry.rootForm && it.locale == otherEntry.locale
}

fun List<L10nEntry>.withLocale(locale: L10nLocale): List<L10nEntry> = filter {
    it.locale == locale
}