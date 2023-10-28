package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.asset.Icons
import javax.swing.Icon

enum class L10nLocale(
    val displayName: String,
    val directoryName: String,
    val icon: Icon
) {

    EN("English", "en-US", Icons.L10N_EN_US),
    RU("Русский", "ru-RU", Icons.L10N_RU_RU);

    companion object {

        fun getByDirectoryName(directoryName: String) = entries.find {
            it.directoryName == directoryName
        }

    }

}