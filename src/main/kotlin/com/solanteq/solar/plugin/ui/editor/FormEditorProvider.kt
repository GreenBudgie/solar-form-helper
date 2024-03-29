package com.solanteq.solar.plugin.ui.editor

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.util.isRootForm

class FormEditorProvider : AsyncFileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile) = file.isRootForm()

    override fun createEditor(project: Project, file: VirtualFile) =
        createEditorAsync(project, file).build()

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
        return object : AsyncFileEditorProvider.Builder() {

            override fun build(): FileEditor {
                return FormEditor(project, file)
            }

        }
    }

    override fun getEditorTypeId() = EDITOR_TYPE_ID

    override fun getPolicy() = FileEditorPolicy.NONE

    companion object {

        const val EDITOR_TYPE_ID = "form-editor"

    }

}