package com.solanteq.solar.plugin.ui.component

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.component.config.FormConfigurationComponent
import com.solanteq.solar.plugin.ui.component.form.RootFormComponent
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.awt.BorderLayout

class FormEditorPanel(
    private val project: Project,
    private val file: JsonFile
) : JBPanel<FormEditorPanel>() {

    private var isUpdateQueued = false

    private val splitter = JBSplitter().apply {
        proportion = DEFAULT_SPLITTER_PROPORTION
        splitterProportionKey = SPLITTER_PROPORTION_KEY
    }
    private val rootFormComponent = RootFormComponent()
    private val configurationComponent = FormConfigurationComponent()

    init {
        layout = BorderLayout()
        val rootFormScrollPane = JBScrollPane(rootFormComponent)
        val configurationScrollPane = JBScrollPane(configurationComponent)
        splitter.firstComponent = rootFormScrollPane
        splitter.secondComponent = configurationScrollPane
        add(splitter, BorderLayout.CENTER)

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
        val form = FormRootFile.createFrom(file) ?: return
        rootFormComponent.update(form)
        configurationComponent.update(form)
    }

    companion object {

        private const val SPLITTER_PROPORTION_KEY = "formEditorSplitterProportion"
        private const val DEFAULT_SPLITTER_PROPORTION = 0.7f

    }

}