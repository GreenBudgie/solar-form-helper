package com.solanteq.solar.plugin.ui.component.form

import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormRootFile
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box

class RootFormComponent : JBPanel<RootFormComponent>() {

    init {
        layout = GridBagLayout()
    }

    fun update(form: FormRootFile) {
        removeAll()

        val strutConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            weightx = 1.0
            weighty = 0.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.FIRST_LINE_START
        }
        val strut = Box.createHorizontalStrut(FORM_WIDTH)
        add(strut, strutConstraints)

        val groupRows = form.groupRows
        val constraints = GridBagConstraints().apply {
            weightx = 1.0
            weighty = 1.0
            gridx = 0
            gridy = 1
            insets = JBUI.insets(FORM_INSETS)
            anchor = GridBagConstraints.FIRST_LINE_START
            fill = GridBagConstraints.HORIZONTAL
        }
        if(groupRows != null) {
            add(GroupRowContainerComponent(groupRows), constraints)
            return
        }
        val groups = form.groups ?: return
        add(GroupContainerComponent(groups), constraints)
    }

    companion object {

        const val FORM_WIDTH = 1440
        const val FORM_INSETS = 24

    }

}