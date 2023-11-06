package com.solanteq.solar.plugin.ui.component

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.FormUIConstants
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

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
        preferredSize = Dimension(FormUIConstants.MIN_VIEWPORT_WIDTH, 0)
        val formRootFile = FormRootFile.createFrom(file) ?: return
        val groupRows = formRootFile.groupRows
        val constraints = GridBagConstraints().apply {
            this.insets = JBUI.insets(24)
            this.fill = GridBagConstraints.BOTH
            this.weightx = 1.0
            this.weighty = 1.0
        }
        if(groupRows != null) {
            add(JBScrollPane(GroupRowContainerComponent(groupRows)), constraints)
            return
        }
        val groups = formRootFile.groups ?: return
        add(GroupContainerComponent(groups), constraints)
    }

}