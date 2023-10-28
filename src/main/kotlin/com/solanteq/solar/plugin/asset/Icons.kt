package com.solanteq.solar.plugin.asset

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Icons {

    val ROOT_FORM_ICON = loadIcon("/assets/icons/root_form.svg")
    val INCLUDED_FORM_ICON = loadIcon("/assets/icons/included_form.svg")
    val L10N_FILE_ICON = loadIcon("/assets/icons/l10n_file.svg")

    val L10N_RU_RU= loadIcon("/assets/icons/l10n_ru_ru.svg")
    val L10N_EN_US = loadIcon("/assets/icons/l10n_en_us.svg")

    private fun loadIcon(path: String): Icon {
        return IconLoader.getIcon(path, Icons::class.java)
    }

}
