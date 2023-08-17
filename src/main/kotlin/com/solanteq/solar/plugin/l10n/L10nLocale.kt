package com.solanteq.solar.plugin.l10n

enum class L10nLocale(
    val displayName: String,
    val directoryName: String
) {

    EN("English", "en-US"),
    RU("Русский", "ru-RU");

    companion object {

        fun getByDirectoryName(directoryName: String) = entries.find {
            it.directoryName == directoryName
        }

    }

}