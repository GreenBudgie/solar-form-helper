package com.solanteq.solar.plugin.ui.component

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormRootFile
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box

class RootFormComponent(
    private val project: Project,
    private val file: JsonFile
) : JBPanel<RootFormComponent>() {

    private var isUpdateQueued = false

    init {
        layout = GridBagLayout()
        update()
    }

    fun update() {
        isUpdateQueued.ifTrue { return }
        isUpdateQueued = true
        DumbService.getInstance(project).runWhenSmart {
            doUpdate()
            isUpdateQueued = false
        }
    }

    private fun doUpdate() {
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

        val formRootFile = FormRootFile.createFrom(file) ?: return
        val groupRows = formRootFile.groupRows
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
        val groups = formRootFile.groups ?: return
        add(GroupContainerComponent(groups), constraints)
    }

    companion object {

        const val FORM_WIDTH = 1440
        const val FORM_INSETS = 24

    }

}