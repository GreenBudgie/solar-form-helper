package com.solanteq.solar.plugin.ui.component.config.expression

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.expression.ExpressionAware
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class ExpressionConfigurationComponent(
    editor: FormEditor,
    form: FormRootFile
) : JBPanel<ExpressionConfigurationComponent>() {

    init {
        layout = GridBagLayout()
        background = JBColor.GREEN
        val expressions = form.expressions ?: emptyList()
        expressions.forEachIndexed { index, expression ->
            val expressionName = expression.name ?: return@forEachIndexed
            if (expressionName == ExpressionAware.NEVER_EXPRESSION) {
                return@forEachIndexed
            }
            val entryConstraints = GridBagConstraints().apply {
                gridx = 0
                gridy = index
                anchor = GridBagConstraints.FIRST_LINE_START
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.HORIZONTAL
            }
            val entry = ExpressionEntryComponent(editor, expressionName)
            add(entry, entryConstraints)
        }
    }

}