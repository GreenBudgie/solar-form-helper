package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.components.JBLabel
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.l10n.L10nLocale

class FieldLabelComponent(
    private val field: FormField
) : JBLabel() {

    init {
        val ruL10nValue = field.getL10nValue(L10nLocale.RU) ?: ""
        text = ruL10nValue
    }

}