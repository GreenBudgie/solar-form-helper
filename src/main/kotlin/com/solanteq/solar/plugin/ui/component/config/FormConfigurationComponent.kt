package com.solanteq.solar.plugin.ui.component.config

import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.component.config.expression.ExpressionConfigurationComponent
import java.awt.BorderLayout
import javax.swing.BorderFactory

class FormConfigurationComponent : JBPanel<FormConfigurationComponent>() {

    init {
        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder(16, 16, 16, 16)
    }

    fun update(form: FormRootFile) {
        removeAll()

        add(ExpressionConfigurationComponent(form), BorderLayout.CENTER)
    }

}