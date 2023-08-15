package com.solanteq.solar.plugin.ui.component

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.toFormElement
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.awt.GridLayout
import javax.swing.BorderFactory

class RootFormComponent(
    private val project: Project,
    private val file: JsonFile
) : JBPanel<RootFormComponent>(), FormComponent {

    private var isUpdateQueued = false
    private val borderGap = 20

    init {
        layout = GridLayout()
        border = BorderFactory.createEmptyBorder(borderGap, borderGap, borderGap, borderGap)
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
        val formRootFile = file.toFormElement<FormRootFile>() ?: return
        val groupRows = formRootFile.groupRows
        if(groupRows != null) {
            add(GroupRowContainerComponent(groupRows))
        }
        val groups = formRootFile.groups ?: return
        add(GroupContainerComponent(groups))
    }

}