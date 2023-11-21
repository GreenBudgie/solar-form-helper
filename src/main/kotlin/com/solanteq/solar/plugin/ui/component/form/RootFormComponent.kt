package com.solanteq.solar.plugin.ui.component.form

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.component.form.base.FormComponent
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box

class RootFormComponent(
    editor: FormEditor,
    form: FormRootFile
) : FormComponent<FormRootFile>(editor, form), Refreshable {

    private val container: Refreshable?

    init {
        layout = GridBagLayout()

        container = buildUI(form)
    }

    override fun refresh() {
        container?.refresh()
        validate()
        repaint()
    }

    private fun buildUI(form: FormRootFile): Refreshable? {
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
            val container = GroupRowContainerComponent(editor, groupRows)
            add(container, constraints)
            return container
        }
        val groups = form.groups ?: return null
        val container = GroupContainerComponent(editor, groups)
        add(container, constraints)
        return container
    }

    companion object {

        const val FORM_WIDTH = 1440
        const val FORM_INSETS = 24

    }

}