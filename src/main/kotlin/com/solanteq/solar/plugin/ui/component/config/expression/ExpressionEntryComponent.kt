package com.solanteq.solar.plugin.ui.component.config.expression

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.util.preferredHeight
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.BorderLayout

class ExpressionEntryComponent(
    private val editor: FormEditor,
    private val expressionName: String
) : JBPanel<ExpressionEntryComponent>() {

    private val expressionCheckbox = JBCheckBox()

    init {
        layout = BorderLayout()
        background = JBColor.RED
        preferredHeight = 30

        val expressionInitialValue = editor.getState().isExpressionTrue(expressionName)
        expressionCheckbox.isSelected = expressionInitialValue
        expressionCheckbox.text = expressionName
        expressionCheckbox.addActionListener { expressionChangedValue() }
        add(expressionCheckbox)
    }

    private fun expressionChangedValue() {
        editor.getState().setExpressionValue(expressionName, !expressionCheckbox.isSelected)
        editor.applyState()
    }

}