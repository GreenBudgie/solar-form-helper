package com.solanteq.solar.plugin.l10n

import com.intellij.ui.IconManager
import com.intellij.ui.LayeredIcon
import com.solanteq.solar.plugin.asset.Icons
import javax.swing.Icon

enum class L10nLocale(
    val displayName: String,
    val directoryName: String,
    val icon: Icon
) {

    EN("English", "en-US", layeredIcon(Icons.L10N_EN_US)),
    RU("Русский", "ru-RU", layeredIcon(Icons.L10N_RU_RU));


    companion object {

        fun getByDirectoryName(directoryName: String) = entries.find {
            it.directoryName == directoryName
        }

    }

}

private fun layeredIcon(icon: Icon): Icon {
    return IconManager.getInstance().createLayered(LayeredIcon.create(Icons.L10N_FILE_ICON, icon))
}