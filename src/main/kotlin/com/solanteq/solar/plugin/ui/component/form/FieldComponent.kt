package com.solanteq.solar.plugin.ui.component.form

import com.intellij.ui.util.preferredHeight
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.ui.FormColorScheme
import com.solanteq.solar.plugin.ui.component.util.UniversalBorder
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.JTextField

class FieldComponent(
    private val field: FormField
) : JPanel() {

    private val fieldComponent = JTextField()

    init {
        layout = GridBagLayout()
        preferredSize = Dimension(0, RowComponent.ROW_HEIGHT)
        val fieldConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.CENTER
            weightx = 1.0
            weighty = 1.0
        }
        fieldComponent.preferredHeight = FIELD_HEIGHT
        fieldComponent.border = UniversalBorder.builder()
            .radius(4)
            .color(FormColorScheme.BORDER_COLOR)
            .build()
        add(fieldComponent, fieldConstraints)
    }

    companion object {

        const val DEFAULT_FIELD_SIZE = 4
        const val FIELD_HEIGHT = 25

    }

}