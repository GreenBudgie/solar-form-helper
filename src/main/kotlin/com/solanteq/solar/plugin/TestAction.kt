package com.solanteq.solar.plugin

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import com.solanteq.solar.plugin.l10n.generator.L10nGenerator

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class TestAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = PsiDocumentManager.getInstance(e.project!!).getPsiFile(editor.document) as? JsonFile ?: return
        L10nGenerator.generateL10n("key", "value", file, L10nGenerator.Placement.endOfFile())
    }

}