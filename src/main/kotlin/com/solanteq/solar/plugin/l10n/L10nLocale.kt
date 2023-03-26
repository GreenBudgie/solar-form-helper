package com.solanteq.solar.plugin.l10n

enum class L10nLocale(
    val displayName: String,
    val directoryName: String
) {

    EN_US("English", "en-US"),
    RU_RU("Русский", "ru-RU");

    companion object {

        fun getByDirectoryName(directoryName: String) = values().find {
            it.directoryName == directoryName
        }

    }

}