package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.GotoTargetHandler.AdditionalAction
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.awt.RelativePoint
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.l10n.action.CreateFormL10nAction
import com.solanteq.solar.plugin.util.asList
import com.solanteq.solar.plugin.util.isForm
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.codeinsight.utils.findExistingEditor
import java.awt.event.MouseEvent
import javax.swing.Icon

class L10nLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is JsonElement) {
            return null
        }
        if (!element.containingFile.isForm()) {
            return null
        }
        val formElement = FormElementFactory.createLocalizableElement(element) ?: return null
        if (formElement.getL10nValues().isEmpty()) {
            return null
        }
        val registerForElement = formElement.namePropertyValue?.firstChild ?: return null
        return LineMarkerInfo(
            registerForElement,
            registerForElement.textRange,
            Icons.L10N_FILE_ICON,
            { "Navigate to localizations" },
            { event, _ -> navigateToL10ns(event, formElement) },
            GutterIconRenderer.Alignment.LEFT,
            { "Navigate to localizations" }
        )
    }

    private fun navigateToL10ns(event: MouseEvent, formElement: FormLocalizableElement<*>) {
        val containingFile = formElement.containingFile ?: return
        val editor = containingFile.findExistingEditor() ?: return
        val localizationsGotoHandler = LocalizationsGotoHandler(formElement)
        localizationsGotoHandler.navigateToLocalizations(formElement.project, editor, containingFile, event)
    }

    class LocalizationsGotoHandler(private val formElement: FormLocalizableElement<*>) : GotoTargetHandler() {

        override fun getFeatureUsedKey() = null

        fun navigateToLocalizations(project: Project, editor: Editor, file: PsiFile, event: MouseEvent) {
            if (DumbService.isDumb(project)) {
                DumbService.getInstance(project).showDumbModeNotification(
                    CodeInsightBundle.message("message.navigation.is.not.available.here.during.index.update")
                )
            }
            val gotoData = getSourceAndTargetElements(editor, file)
            if (gotoData != null) {
                show(project, editor, file, gotoData) { popup -> popup.show(RelativePoint(event)) }
            }
        }

        override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
            val source = formElement.namePropertyValue ?: return null
            val l10ns = getDistinctL10ns(formElement.getL10ns())
            val targets = l10ns.map { it.property }
            val createL10nAdditionalAction = AddL10nAdditionalAction(
                formElement.project,
                editor,
                formElement.sourceElement
            )
            return GotoData(source, targets.toTypedArray(), createL10nAdditionalAction.asList())
        }

        override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
           return "No localizations found"
        }

        override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
            sourceElement as JsonStringLiteral
            return "Localizations for ${sourceElement.value} ($length found)"
        }

        /**
         * Sometimes there are multiple identical l10ns in libraries (if multiple versions of the same
         * artifact are present in project). This method will hide these l10ns from libraries, so only
         * project l10ns remain.
         */
        private fun getDistinctL10ns(l10ns: List<L10n>): List<L10n> {
            val projectScope = formElement.project.projectScope()
            val (l10nsInProject, l10nsInLibraries) = l10ns.partition { it.file.virtualFile in projectScope }
            val distinctL10nsInLibraries = l10nsInLibraries.filter { it !in l10nsInProject }
            return l10nsInProject + distinctL10nsInLibraries
        }

    }

    private class AddL10nAdditionalAction(
        private val project: Project,
        private val editor: Editor,
        private val element: JsonElement,
    ) : AdditionalAction {

        override fun getText() = "Add Localization"

        override fun getIcon() = Icons.NEW_L10N_ACTION

        override fun execute() {
            val action = CreateFormL10nAction()
            if (action.isAvailable(project, editor, element)) {
                action.invoke(project, editor, element)
            }
        }

    }

}