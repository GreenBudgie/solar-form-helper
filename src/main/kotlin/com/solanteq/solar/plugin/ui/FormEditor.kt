package com.solanteq.solar.plugin.ui

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.solanteq.solar.plugin.ui.component.RootFormComponent
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class FormEditor(
    private val project: Project,
    private val virtualFile: VirtualFile
) : FileEditor {

    private val file = virtualFile.toPsiFile(project) as? JsonFile ?: error("No JsonFile is backing the virtual file")
    private val rootFormComponent = RootFormComponent(project, file)
    private val mainScrollPane = JBScrollPane(rootFormComponent)

    override fun getFile() = virtualFile

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return null
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {

    }

    override fun dispose() {

    }

    override fun getComponent(): JComponent {
        return mainScrollPane
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun getName() = "Form Preview"

    override fun setState(state: FileEditorState) {

    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun selectNotify() {
        rootFormComponent.update()
    }

}