package com.solanteq.solar.plugin.settings

import com.intellij.codeInsight.actions.ReaderModeSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.hints.InlayHintsFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.psi.PsiDocumentManager
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.util.isForm

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class LocaleToggleAction : DumbAwareToggleAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent) = true

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val newLocale = when (service<SolarProjectConfiguration>().state.locale) {
            L10nLocale.EN -> L10nLocale.RU
            L10nLocale.RU -> L10nLocale.EN
        }

        service<SolarProjectConfiguration>().state.locale = newLocale
        val project = e.project ?: return
        InlayHintsFactory.forceHintsUpdateOnNextPass()
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        val isForm = file.isForm()
        if (!isForm) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        e.presentation.isEnabledAndVisible = true

        val locale = service<SolarProjectConfiguration>().state.locale
        e.presentation.icon = locale.icon
        e.presentation.text = "Locale: ${locale.directoryName}"
    }

}