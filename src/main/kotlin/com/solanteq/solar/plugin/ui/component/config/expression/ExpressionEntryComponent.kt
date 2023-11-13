package com.solanteq.solar.plugin.ui.component.config.expression

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.util.preferredHeight
import com.solanteq.solar.plugin.element.expression.FormExpression
import java.awt.BorderLayout

class ExpressionEntryComponent(
    expression: FormExpression
) : JBPanel<ExpressionEntryComponent>() {

    init {
        layout = BorderLayout()
        background = JBColor.RED
        preferredHeight = 30
        val expressionName = expression.name ?: "unnamed"
        val checkbox = JBCheckBox(expressionName)
        add(checkbox)
    }

}