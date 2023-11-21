package com.solanteq.solar.plugin.ui.editor

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.ui.component.FormEditorPanel
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class FormEditor(
    project: Project,
    private val virtualFile: VirtualFile
) : FileEditor {

    private val userDataHolder = UserDataHolderBase()
    private var state = FormEditorState()

    private val file = virtualFile.toPsiFile(project) as? JsonFile ?: error("No JsonFile is backing the virtual file")
    private val editorPanel = FormEditorPanel(this, project, file)

    override fun getFile() = virtualFile

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return userDataHolder.getUserData(key)
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userDataHolder.putUserData(key, value)
    }

    override fun dispose() {

    }

    override fun getComponent(): JComponent {
        return editorPanel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun getName() = "Form Preview"

    override fun setState(state: FileEditorState) {
        val newState = state as? FormEditorState ?: FormEditorState()
        this.state = newState
    }

    override fun getState(level: FileEditorStateLevel): FormEditorState {
        return state
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
        editorPanel.rebuildIfNeeded()
    }

    fun getState(): FormEditorState {
        return state
    }

    fun applyState() {
        editorPanel.refresh()
    }

}