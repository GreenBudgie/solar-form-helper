package com.solanteq.solar.plugin.ui.component

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.ui.component.config.FormConfigurationComponent
import com.solanteq.solar.plugin.ui.component.form.RootFormComponent
import com.solanteq.solar.plugin.ui.editor.FormEditor
import com.solanteq.solar.plugin.util.jsonModificationTracker
import java.awt.BorderLayout

class FormEditorPanel(
    private val editor: FormEditor,
    private val project: Project,
    private val file: JsonFile
) : JBPanel<FormEditorPanel>() {

    private val modificationTracker = project.jsonModificationTracker()
    private var prevModificationCount = INITIAL_MODIFICATION_TRACKER_VALUE

    private val splitter = OnePixelSplitter().apply {
        proportion = DEFAULT_SPLITTER_PROPORTION
        splitterProportionKey = SPLITTER_PROPORTION_KEY
    }
    private var rootFormComponent: RootFormComponent? = null
    private var configurationComponent: FormConfigurationComponent? = null

    init {
        layout = BorderLayout()

        rebuildIfNeeded()
    }

    /**
     * Refreshes the form UI without getting updates from the file.
     * @see com.solanteq.solar.plugin.ui.component.util.Refreshable.refresh
     */
    fun refresh() {
        DumbService.getInstance(project).runWhenSmart {
            doRefresh()
        }
    }

    /**
     * Fully updates form UI and configuration by constructing new [FormRootFile] if there were PSI changes
     */
    fun rebuildIfNeeded() {
        DumbService.getInstance(project).runWhenSmart {
            if (!needToRebuild()) {
                return@runWhenSmart
            }
            doRebuild()
            prevModificationCount = modificationTracker.modificationCount
        }
    }

    private fun needToRebuild(): Boolean {
        return prevModificationCount == INITIAL_MODIFICATION_TRACKER_VALUE ||
            prevModificationCount != modificationTracker.modificationCount
    }

    private fun doRebuild() {
        val form = FormRootFile.createFrom(file) ?: return

        rootFormComponent = RootFormComponent(editor, form)
        configurationComponent = FormConfigurationComponent(editor, form)

        val rootFormScrollPane = JBScrollPane(rootFormComponent)
        val configurationScrollPane = JBScrollPane(configurationComponent)
        splitter.firstComponent = rootFormScrollPane
        splitter.secondComponent = configurationScrollPane
        add(splitter, BorderLayout.CENTER)
    }

    private fun doRefresh() {
        rootFormComponent?.refresh()
    }

    companion object {

        private const val INITIAL_MODIFICATION_TRACKER_VALUE = -1L

        private const val SPLITTER_PROPORTION_KEY = "formEditorSplitterProportion"
        private const val DEFAULT_SPLITTER_PROPORTION = 0.7f

    }

}