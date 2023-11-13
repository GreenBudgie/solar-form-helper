package com.solanteq.solar.plugin.ui.component

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.l10n.L10nLocale
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel

class FieldLabelComponent(
    private val field: FormField
) : JPanel() {

    init {
        layout = GridBagLayout()
        preferredSize = Dimension(0, RowComponent.ROW_HEIGHT)

        val ruL10nValue = field.getL10nValue(L10nLocale.RU) ?: ""

        val labelComponent = JLabel("<html><div style=\"text-align:right;\">$ruL10nValue</div></html>").apply {
            font = font.deriveFont(12f)
        }

        val labelConstraints = GridBagConstraints().apply {
            anchor = GridBagConstraints.LINE_END
            insets = JBUI.insetsRight(RIGHT_LABEL_INSET)
            weightx = 1.0
        }
        add(labelComponent, labelConstraints)
    }

    companion object {

        const val RIGHT_LABEL_INSET = 10
        const val DEFAULT_LABEL_SIZE = 2

    }

}