package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.element.FormRootFile

data class L10nKey(
    val rootForm: FormRootFile,
    val key: String,
    val locale: L10nLocale
)