package com.solanteq.solar.plugin.ui.component.config

import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.component.config.expression.ExpressionConfigurationComponent
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory

class FormConfigurationComponent(
    editor: FormEditor,
    form: FormRootFile
) : JBPanel<FormConfigurationComponent>() {

    init {
        layout = GridBagLayout()
        border = BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE)

        val constrains = GridBagConstraints().apply {
            weightx = 1.0
            weighty = 1.0
            anchor = GridBagConstraints.FIRST_LINE_START
            fill = GridBagConstraints.HORIZONTAL
        }
        add(ExpressionConfigurationComponent(editor, form), constrains)
    }

    companion object {

        const val BORDER_SIZE = 16

    }

}