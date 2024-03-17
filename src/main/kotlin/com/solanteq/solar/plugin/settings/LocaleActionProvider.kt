package com.solanteq.solar.plugin.settings

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider

class LocaleActionProvider : InspectionWidgetActionProvider {

    override fun createAction(editor: Editor): AnAction? {
        val project = editor.project ?: return null
        return object : DefaultActionGroup(LocaleToggleAction(), Separator.create()) {

            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        }
    }

}